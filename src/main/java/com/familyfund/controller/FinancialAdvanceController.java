package com.familyfund.controller;

import com.familyfund.model.FinancialAdvance;
import com.familyfund.service.FinancialAdvanceService;
import com.familyfund.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/advances")
@RequiredArgsConstructor
public class FinancialAdvanceController {

    private final FinancialAdvanceService advanceService;
    private final MemberService memberService;

    @GetMapping
    public String listAdvances(Model model) {
        model.addAttribute("activeAdvances", advanceService.getActiveAdvances());
        model.addAttribute("activeMembers", memberService.getActiveMembers());
        return "advances/list";
    }

    @GetMapping("/{id}")
    public String viewAdvance(@PathVariable Long id, Model model) {
        model.addAttribute("advance", advanceService.getAdvanceById(id));
        return "advances/view";
    }

    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createAdvance(@RequestBody Map<String, String> payload) {
        try {
            Long memberId = Long.parseLong(payload.get("memberId"));
            BigDecimal amount = new BigDecimal(payload.get("amount"));
            String purpose = payload.get("purpose");

            FinancialAdvance advance = advanceService.createAdvance(memberId, amount, purpose);
            return ResponseEntity.ok().body(Map.of("id", advance.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/payment")
    @ResponseBody
    public ResponseEntity<?> recordPayment(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            BigDecimal amount = new BigDecimal(payload.get("amount"));
            advanceService.recordPayment(id, amount);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelAdvance(@PathVariable Long id) {
        try {
            advanceService.cancelAdvance(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 