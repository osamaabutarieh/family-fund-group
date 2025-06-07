package com.familyfund.repository;

import com.familyfund.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByMemberId(Long memberId);
    Optional<Subscription> findByMemberIdAndMonth(Long memberId, YearMonth month);
    List<Subscription> findByMonth(YearMonth month);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.month = ?1 AND s.paid = true")
    long countPaidSubscriptionsForMonth(YearMonth month);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.month = ?1 AND s.paid = false")
    long countUnpaidSubscriptionsForMonth(YearMonth month);
} 