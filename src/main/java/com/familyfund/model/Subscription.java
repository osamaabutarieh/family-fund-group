package com.familyfund.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@Entity
@Table(name = "subscriptions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "subscription_month"}))
public class Subscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(name = "subscription_month", nullable = false)
    private YearMonth month;
    
    @Column(nullable = false)
    private boolean paid = false;
    
    private LocalDate paymentDate;
    
    @PrePersist
    @PreUpdate
    private void validatePaymentDate() {
        if (paid && paymentDate == null) {
            paymentDate = LocalDate.now();
        }
    }
} 