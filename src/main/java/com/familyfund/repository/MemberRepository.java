package com.familyfund.repository;

import com.familyfund.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByStatus(Member.MemberStatus status);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
} 