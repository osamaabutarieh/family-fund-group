package com.familyfund.service;

import com.familyfund.model.Member;
import com.familyfund.model.Subscription;
import com.familyfund.model.Transaction;
import com.familyfund.repository.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final MemberService memberService;
    private final TransactionService transactionService;
    
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }
    
    public List<Subscription> getSubscriptionsByMember(Long memberId) {
        return subscriptionRepository.findByMemberId(memberId);
    }
    
    public List<Subscription> getSubscriptionsByMonth(YearMonth month) {
        return subscriptionRepository.findByMonth(month);
    }
    
    @Transactional
    public Subscription recordPayment(Long memberId, YearMonth month, BigDecimal amount, Transaction.PaymentType paymentType) {
        Member member = memberService.getMemberById(memberId);
        
        Subscription subscription = subscriptionRepository
            .findByMemberIdAndMonth(memberId, month)
            .orElseGet(() -> {
                Subscription newSub = new Subscription();
                newSub.setMember(member);
                newSub.setMonth(month);
                return newSub;
            });
        
        subscription.setPaid(true);
        subscription.setPaymentDate(LocalDate.now());
        
        // Record the transaction
        Transaction transaction = new Transaction();
        transaction.setMember(member);
        transaction.setAmount(amount);
        transaction.setType(Transaction.TransactionType.CREDIT);
        transaction.setPaymentType(paymentType != null ? paymentType : Transaction.PaymentType.SUBSCRIPTION);
        transaction.setNote("Payment for " + month + " (" + transaction.getPaymentType().getDisplayName() + ")");
        transactionService.createTransaction(transaction);
        
        return subscriptionRepository.save(subscription);
    }
    
    @Transactional
    public Subscription recordPayment(Long memberId, YearMonth month, BigDecimal amount) {
        return recordPayment(memberId, month, amount, Transaction.PaymentType.SUBSCRIPTION);
    }
    
    public long getPaidSubscriptionsCount(YearMonth month) {
        return subscriptionRepository.countPaidSubscriptionsForMonth(month);
    }
    
    public long getUnpaidSubscriptionsCount(YearMonth month) {
        return subscriptionRepository.countUnpaidSubscriptionsForMonth(month);
    }
    
    @Transactional
    public void cancelPayment(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + subscriptionId));
        
        // Find the payment transaction for this subscription
        LocalDateTime startOfDay = subscription.getPaymentDate().atStartOfDay();
        LocalDateTime endOfDay = subscription.getPaymentDate().plusDays(1).atStartOfDay().minusSeconds(1);
        
        List<Transaction> transactions = transactionService.getTransactionsBetweenDates(startOfDay, endOfDay)
            .stream()
            .filter(t -> t.getMember().getId().equals(subscription.getMember().getId())
                && t.getType() == Transaction.TransactionType.CREDIT
                && t.getPaymentType() == Transaction.PaymentType.SUBSCRIPTION)
            .toList();
        
        if (!transactions.isEmpty()) {
            // Create reversal transaction
            Transaction reversal = new Transaction();
            reversal.setMember(subscription.getMember());
            reversal.setAmount(transactions.get(0).getAmount()); // Use the same amount as the original payment
            reversal.setType(Transaction.TransactionType.DEBIT);
            reversal.setPaymentType(Transaction.PaymentType.SUBSCRIPTION);
            reversal.setNote("Reversal of subscription payment for " + subscription.getMonth());
            transactionService.createTransaction(reversal);
        }
        
        // Update subscription status
        subscription.setPaid(false);
        subscription.setPaymentDate(null);
        subscriptionRepository.save(subscription);
    }
} 