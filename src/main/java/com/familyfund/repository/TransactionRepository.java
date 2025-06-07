package com.familyfund.repository;

import com.familyfund.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByMemberIdOrderByDateDesc(Long memberId);
    List<Transaction> findByDateBetweenOrderByDateDesc(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'CREDIT'")
    BigDecimal getTotalCredits();
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'DEBIT'")
    BigDecimal getTotalDebits();
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'CREDIT' AND t.date BETWEEN :start AND :end")
    BigDecimal getTotalCreditsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.type = 'DEBIT' AND t.date BETWEEN :start AND :end")
    BigDecimal getTotalDebitsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
} 