package com.familyfund.service;

import com.familyfund.model.Transaction;
import com.familyfund.repository.TransactionRepository;
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
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
    }
    
    public List<Transaction> getTransactionsByMember(Long memberId) {
        return transactionRepository.findByMemberIdOrderByDateDesc(memberId);
    }
    
    public List<Transaction> getTransactionsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByDateBetweenOrderByDateDesc(start, end);
    }
    
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDateTime.now());
        }
        return transactionRepository.save(transaction);
    }
    
    public BigDecimal getTotalBalance() {
        BigDecimal totalCredits = transactionRepository.getTotalCredits();
        BigDecimal totalDebits = transactionRepository.getTotalDebits();
        
        totalCredits = totalCredits == null ? BigDecimal.ZERO : totalCredits;
        totalDebits = totalDebits == null ? BigDecimal.ZERO : totalDebits;
        
        return totalCredits.subtract(totalDebits);
    }
    
    public BigDecimal getBalanceForPeriod(LocalDateTime start, LocalDateTime end) {
        BigDecimal credits = transactionRepository.getTotalCreditsBetween(start, end);
        BigDecimal debits = transactionRepository.getTotalDebitsBetween(start, end);
        
        credits = credits == null ? BigDecimal.ZERO : credits;
        debits = debits == null ? BigDecimal.ZERO : debits;
        
        return credits.subtract(debits);
    }

    public BigDecimal getTotalCreditsBetween(LocalDateTime start, LocalDateTime end) {
        BigDecimal credits = transactionRepository.getTotalCreditsBetween(start, end);
        return credits == null ? BigDecimal.ZERO : credits;
    }

    public BigDecimal getTotalDebitsBetween(LocalDateTime start, LocalDateTime end) {
        BigDecimal debits = transactionRepository.getTotalDebitsBetween(start, end);
        return debits == null ? BigDecimal.ZERO : debits;
    }
} 