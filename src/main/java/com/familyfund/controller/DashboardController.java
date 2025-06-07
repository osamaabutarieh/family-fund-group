package com.familyfund.controller;

import com.familyfund.model.Member;
import com.familyfund.model.Subscription;
import com.familyfund.model.Transaction;
import com.familyfund.service.MemberService;
import com.familyfund.service.SubscriptionService;
import com.familyfund.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final MemberService memberService;
    private final SubscriptionService subscriptionService;
    private final TransactionService transactionService;

    @GetMapping("/")
    public String dashboard(Model model) {
        // Get total members and inactive members
        long totalMembers = memberService.getAllMembers().size();
        long inactiveMembers = memberService.getAllMembers().stream()
                .filter(m -> m.getStatus() == Member.MemberStatus.INACTIVE)
                .count();

        // Get current month's subscriptions
        YearMonth currentMonth = YearMonth.now();
        long pendingPayments = subscriptionService.getUnpaidSubscriptionsCount(currentMonth);

        // Get total funds (balance)
        var totalFunds = transactionService.getTotalBalance();

        // Get recent transactions
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        var recentTransactions = transactionService.getTransactionsBetweenDates(oneMonthAgo, LocalDateTime.now());

        // Get pending subscriptions
        var pendingSubscriptions = subscriptionService.getSubscriptionsByMonth(currentMonth).stream()
                .filter(s -> !s.isPaid())
                .toList();

        // Add attributes to model
        model.addAttribute("totalMembers", totalMembers);
        model.addAttribute("inactiveMembers", inactiveMembers);
        model.addAttribute("pendingPayments", pendingPayments);
        model.addAttribute("totalFunds", totalFunds);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("pendingSubscriptions", pendingSubscriptions);

        return "dashboard";
    }
} 