package com.familyfund.repository;

import com.familyfund.model.FinancialAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FinancialAdvanceRepository extends JpaRepository<FinancialAdvance, Long> {
    List<FinancialAdvance> findByMemberIdOrderByIssueDateDesc(Long memberId);
    List<FinancialAdvance> findByStatusOrderByIssueDateDesc(FinancialAdvance.AdvanceStatus status);
} 