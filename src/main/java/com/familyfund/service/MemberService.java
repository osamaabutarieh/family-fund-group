package com.familyfund.service;

import com.familyfund.model.Member;
import com.familyfund.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    
    private final MemberRepository memberRepository;
    
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }
    
    public List<Member> getActiveMembers() {
        return memberRepository.findByStatus(Member.MemberStatus.ACTIVE);
    }
    
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + id));
    }
    
    @Transactional
    public Member createMember(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (memberRepository.existsByPhone(member.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        
        member.setJoinDate(LocalDate.now());
        member.setStatus(Member.MemberStatus.ACTIVE);
        return memberRepository.save(member);
    }
    
    @Transactional
    public Member updateMember(Long id, Member memberDetails) {
        Member member = getMemberById(id);
        
        if (!member.getEmail().equals(memberDetails.getEmail()) && 
            memberRepository.existsByEmail(memberDetails.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!member.getPhone().equals(memberDetails.getPhone()) && 
            memberRepository.existsByPhone(memberDetails.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        
        member.setName(memberDetails.getName());
        member.setEmail(memberDetails.getEmail());
        member.setPhone(memberDetails.getPhone());
        member.setStatus(memberDetails.getStatus());
        
        return memberRepository.save(member);
    }
    
    @Transactional
    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        member.setStatus(Member.MemberStatus.INACTIVE);
        memberRepository.save(member);
    }
} 