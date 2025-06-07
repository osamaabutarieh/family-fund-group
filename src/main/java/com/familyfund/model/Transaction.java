package com.familyfund.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(length = 500)
    private String note;
    
    public enum TransactionType {
        CREDIT, // Money coming in (e.g., subscription payments)
        DEBIT   // Money going out (e.g., expenses)
    }
    
    public enum PaymentType {
        SUBSCRIPTION("Subscription Payment"),
        ADVANCE("Advance Payment"),
        DONATION("Donation"),
        EXPENSE("Expense"),
        OTHER("Other");
        
        private final String displayName;
        
        PaymentType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (date == null) {
            date = LocalDateTime.now();
        }
        if (paymentType == null && type == TransactionType.DEBIT) {
            paymentType = PaymentType.EXPENSE;
        }
    }
} 