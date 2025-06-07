package com.familyfund.controller;

import com.familyfund.model.Member;
import com.familyfund.model.Transaction;
import com.familyfund.service.MemberService;
import com.familyfund.service.SubscriptionService;
import com.familyfund.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@Controller
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final MemberService memberService;
    private final TransactionService transactionService;

    @GetMapping
    public String listSubscriptions(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            Model model) {
        
        // If month is not provided, use current month
        YearMonth selectedMonth = month != null ? month : YearMonth.now();
        
        // Get active members for the payment modal
        var activeMembers = memberService.getActiveMembers();
        
        // Get subscriptions for the selected month
        var subscriptions = subscriptionService.getSubscriptionsByMonth(selectedMonth);
        
        // Calculate statistics
        long totalMembers = activeMembers.size();
        long paidCount = subscriptionService.getPaidSubscriptionsCount(selectedMonth);
        long unpaidCount = subscriptionService.getUnpaidSubscriptionsCount(selectedMonth);
        
        // Get total amount collected for the month
        var startDate = selectedMonth.atDay(1).atStartOfDay();
        var endDate = selectedMonth.atEndOfMonth().atTime(23, 59, 59);
        var totalAmount = transactionService.getBalanceForPeriod(startDate, endDate);

        // Add attributes to model
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("activeMembers", activeMembers);
        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("totalAmount", totalAmount);

        return "subscriptions/list";
    }

    @PostMapping("/record-payment")
    @ResponseBody
    public ResponseEntity<?> recordPayment(@RequestBody Map<String, String> payload) {
        try {
            Long memberId = Long.parseLong(payload.get("memberId"));
            BigDecimal amount = new BigDecimal(payload.get("amount"));
            YearMonth month = YearMonth.parse(payload.get("month"));
            Transaction.PaymentType paymentType = Transaction.PaymentType.valueOf(payload.get("paymentType"));

            subscriptionService.recordPayment(memberId, month, amount, paymentType);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelPayment(@PathVariable Long id) {
        try {
            subscriptionService.cancelPayment(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 