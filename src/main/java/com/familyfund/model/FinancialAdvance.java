package com.familyfund.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_advances")
@Data
public class FinancialAdvance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    @Column(nullable = false)
    private BigDecimal remainingAmount;
    
    @Column(nullable = false)
    private LocalDateTime issueDate;
    
    @Column(length = 500)
    private String purpose;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AdvanceStatus status;
    
    private LocalDateTime completionDate;
    
    public enum AdvanceStatus {
        ACTIVE("Active"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");
        
        private final String displayName;
        
        AdvanceStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (issueDate == null) {
            issueDate = LocalDateTime.now();
        }
        if (status == null) {
            status = AdvanceStatus.ACTIVE;
        }
        if (remainingAmount == null) {
            remainingAmount = totalAmount;
        }
    }
} 