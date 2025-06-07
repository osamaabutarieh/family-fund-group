package com.familyfund.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "members")
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @Column(nullable = false)
    private LocalDate joinDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;
    
    public enum MemberStatus {
        ACTIVE, INACTIVE
    }
} 