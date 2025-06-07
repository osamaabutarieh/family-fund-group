package com.familyfund.service;

import com.familyfund.model.FinancialAdvance;
import com.familyfund.model.Member;
import com.familyfund.model.Transaction;
import com.familyfund.repository.FinancialAdvanceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinancialAdvanceService {
    
    private final FinancialAdvanceRepository advanceRepository;
    private final MemberService memberService;
    private final TransactionService transactionService;
    
    public List<FinancialAdvance> getAllAdvances() {
        return advanceRepository.findAll();
    }
    
    public List<FinancialAdvance> getActiveAdvances() {
        return advanceRepository.findByStatusOrderByIssueDateDesc(FinancialAdvance.AdvanceStatus.ACTIVE);
    }
    
    public List<FinancialAdvance> getMemberAdvances(Long memberId) {
        return advanceRepository.findByMemberIdOrderByIssueDateDesc(memberId);
    }
    
    public FinancialAdvance getAdvanceById(Long id) {
        return advanceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Financial advance not found with id: " + id));
    }
    
    @Transactional
    public FinancialAdvance createAdvance(Long memberId, BigDecimal amount, String purpose) {
        Member member = memberService.getMemberById(memberId);
        
        FinancialAdvance advance = new FinancialAdvance();
        advance.setMember(member);
        advance.setTotalAmount(amount);
        advance.setRemainingAmount(amount);
        advance.setPurpose(purpose);
        advance.setStatus(FinancialAdvance.AdvanceStatus.ACTIVE);
        
        // Record the transaction
        Transaction transaction = new Transaction();
        transaction.setMember(member);
        transaction.setAmount(amount);
        transaction.setType(Transaction.TransactionType.DEBIT);
        transaction.setPaymentType(Transaction.PaymentType.ADVANCE);
        transaction.setNote("Financial advance issued: " + purpose);
        transactionService.createTransaction(transaction);
        
        return advanceRepository.save(advance);
    }
    
    @Transactional
    public FinancialAdvance recordPayment(Long advanceId, BigDecimal amount) {
        FinancialAdvance advance = getAdvanceById(advanceId);
        
        if (advance.getStatus() != FinancialAdvance.AdvanceStatus.ACTIVE) {
            throw new IllegalStateException("Cannot record payment for non-active advance");
        }
        
        if (amount.compareTo(advance.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount cannot be greater than remaining amount");
        }
        
        // Record the payment transaction
        Transaction transaction = new Transaction();
        transaction.setMember(advance.getMember());
        transaction.setAmount(amount);
        transaction.setType(Transaction.TransactionType.CREDIT);
        transaction.setPaymentType(Transaction.PaymentType.ADVANCE);
        transaction.setNote("Payment towards financial advance: " + advance.getPurpose());
        transactionService.createTransaction(transaction);
        
        // Update advance
        advance.setRemainingAmount(advance.getRemainingAmount().subtract(amount));
        
        // Check if advance is fully paid
        if (advance.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            advance.setStatus(FinancialAdvance.AdvanceStatus.COMPLETED);
            advance.setCompletionDate(LocalDateTime.now());
        }
        
        return advanceRepository.save(advance);
    }
    
    @Transactional
    public FinancialAdvance cancelAdvance(Long advanceId) {
        FinancialAdvance advance = getAdvanceById(advanceId);
        
        if (advance.getStatus() != FinancialAdvance.AdvanceStatus.ACTIVE) {
            throw new IllegalStateException("Cannot cancel non-active advance");
        }
        
        advance.setStatus(FinancialAdvance.AdvanceStatus.CANCELLED);
        advance.setCompletionDate(LocalDateTime.now());
        
        return advanceRepository.save(advance);
    }
} 