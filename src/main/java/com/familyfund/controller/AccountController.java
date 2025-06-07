package com.familyfund.controller;

import com.familyfund.dto.DashboardData;
import com.familyfund.model.Member;
import com.familyfund.model.Transaction;
import com.familyfund.service.ExportService;
import com.familyfund.service.MemberService;
import com.familyfund.service.TransactionService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final TransactionService transactionService;
    private final MemberService memberService;
    private final ExportService exportService;

    @GetMapping
    public String showAccounts(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        // If dates not provided, use current month
        LocalDateTime start = startDate != null ? 
            startDate.atStartOfDay() : 
            LocalDate.now().withDayOfMonth(1).atStartOfDay();
            
        LocalDateTime end = endDate != null ? 
            endDate.atTime(LocalTime.MAX) : 
            LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);

        // Get transactions for the period
        List<Transaction> transactions = transactionService.getTransactionsBetweenDates(start, end);

        // Calculate monthly statistics
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        
        BigDecimal monthlyIncome = transactionService.getTotalCreditsBetween(monthStart, monthEnd);
        BigDecimal monthlyExpenses = transactionService.getTotalDebitsBetween(monthStart, monthEnd);
        BigDecimal totalBalance = transactionService.getTotalBalance();
        BigDecimal pendingAmount = BigDecimal.valueOf(50.00) // Assuming $50 is the standard subscription fee
                .multiply(BigDecimal.valueOf(memberService.getActiveMembers().size()));

        // Add attributes to model
        model.addAttribute("transactions", transactions);
        model.addAttribute("members", memberService.getAllMembers());
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("monthlyExpenses", monthlyExpenses);
        model.addAttribute("pendingAmount", pendingAmount);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "accounts/overview";
    }

    @GetMapping("/transactions/{id}")
    public String showTransactionDetails(@PathVariable Long id, Model model) {
        Transaction transaction = transactionService.getTransactionById(id);
        List<Transaction> relatedTransactions = transaction.getMember() != null ?
            transactionService.getTransactionsByMember(transaction.getMember().getId()) :
            List.of();

        model.addAttribute("transaction", transaction);
        model.addAttribute("relatedTransactions", relatedTransactions);

        return "accounts/transaction-details";
    }

    @PostMapping("/transactions")
    @ResponseBody
    public ResponseEntity<?> createTransaction(@RequestBody Map<String, String> payload) {
        try {
            Transaction transaction = new Transaction();
            transaction.setType(Transaction.TransactionType.valueOf(payload.get("type")));
            transaction.setAmount(new BigDecimal(payload.get("amount")));
            transaction.setNote(payload.get("note"));

            String memberId = payload.get("memberId");
            if (memberId != null && !memberId.isEmpty()) {
                Member member = memberService.getMemberById(Long.parseLong(memberId));
                transaction.setMember(member);
            }

            transactionService.createTransaction(transaction);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam String format,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Transaction.TransactionType type) throws Exception {
        
        // If dates not provided, use current month
        LocalDateTime start = startDate != null ? 
            startDate.atStartOfDay() : 
            LocalDate.now().withDayOfMonth(1).atStartOfDay();
            
        LocalDateTime end = endDate != null ? 
            endDate.atTime(LocalTime.MAX) : 
            LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);

        // Get transactions
        List<Transaction> transactions = transactionService.getTransactionsBetweenDates(start, end);
        if (type != null) {
            transactions = transactions.stream()
                .filter(t -> t.getType() == type)
                .toList();
        }

        // Generate title
        String title = String.format("Transactions_%s_to_%s", 
            startDate != null ? startDate.toString() : "start",
            endDate != null ? endDate.toString() : "end");

        // Export based on format
        byte[] content;
        String contentType;
        String filename;

        if ("excel".equalsIgnoreCase(format)) {
            content = exportService.exportToExcel(transactions, "Transactions");
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            filename = title + ".xlsx";
        } else if ("pdf".equalsIgnoreCase(format)) {
            content = exportService.exportToPdf(transactions, "Family Fund Transactions");
            contentType = MediaType.APPLICATION_PDF_VALUE;
            filename = title + ".pdf";
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(content);
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Get current date and start of month
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        // Calculate basic metrics
        List<Member> activeMembers = memberService.getActiveMembers();
        BigDecimal totalBalance = transactionService.getTotalBalance();
        BigDecimal monthlyIncome = transactionService.getTotalCreditsBetween(startOfMonth, now);
        int unpaidCount = calculateUnpaidSubscriptions();

        // Generate monthly chart data
        DashboardData.MonthlyChartData monthlyData = generateMonthlyChartData();

        // Get transaction distribution
        int[] distribution = calculateTransactionDistribution();

        // Get recent transactions
        List<Transaction> recentTransactions = transactionService.getTransactionsBetweenDates(
            startOfMonth, now).stream().limit(10).toList();

        // Get top contributors
        List<DashboardData.TopContributor> topContributors = calculateTopContributors();

        // Calculate monthly average (last 6 months)
        BigDecimal monthlyAverage = calculateMonthlyAverage();

        // Add all data to model
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("activeMembers", activeMembers.size());
        model.addAttribute("monthlyAverage", monthlyAverage);
        model.addAttribute("unpaidCount", unpaidCount);
        model.addAttribute("monthlyData", monthlyData);
        model.addAttribute("transactionDistribution", distribution);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("topContributors", topContributors);

        return "accounts/dashboard";
    }

    private int calculateUnpaidSubscriptions() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now()
            .withDayOfMonth(LocalDate.now().lengthOfMonth())
            .atTime(LocalTime.MAX);

        List<Member> activeMembers = memberService.getActiveMembers();
        List<Transaction> monthlySubscriptions = transactionService
            .getTransactionsBetweenDates(startOfMonth, endOfMonth)
            .stream()
            .filter(t -> t.getType() == Transaction.TransactionType.CREDIT)
            .toList();

        return (int) activeMembers.stream()
            .filter(member -> monthlySubscriptions.stream()
                .noneMatch(t -> t.getMember() != null && t.getMember().getId().equals(member.getId())))
            .count();
    }

    private DashboardData.MonthlyChartData generateMonthlyChartData() {
        DashboardData.MonthlyChartData data = new DashboardData.MonthlyChartData();
        
        // Get last 6 months
        String[] labels = new String[6];
        BigDecimal[] income = new BigDecimal[6];
        BigDecimal[] expenses = new BigDecimal[6];

        LocalDate now = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            LocalDateTime start = date.withDayOfMonth(1).atStartOfDay();
            LocalDateTime end = date.withDayOfMonth(date.lengthOfMonth()).atTime(LocalTime.MAX);

            labels[5-i] = date.format(monthFormatter);
            income[5-i] = transactionService.getTotalCreditsBetween(start, end);
            expenses[5-i] = transactionService.getTotalDebitsBetween(start, end);
        }

        data.setLabels(labels);
        data.setIncome(income);
        data.setExpenses(expenses);

        return data;
    }

    private int[] calculateTransactionDistribution() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Transaction> transactions = transactionService.getTransactionsBetweenDates(
            sixMonthsAgo, LocalDateTime.now());

        int[] distribution = new int[4]; // [Subscriptions, Donations, Expenses, Other]

        for (Transaction t : transactions) {
            if (t.getType() == Transaction.TransactionType.DEBIT) {
                distribution[2]++; // Expenses
            } else if (t.getAmount().compareTo(BigDecimal.valueOf(50)) == 0) {
                distribution[0]++; // Subscriptions (assuming $50 is the standard fee)
            } else if (t.getAmount().compareTo(BigDecimal.valueOf(50)) > 0) {
                distribution[1]++; // Donations
            } else {
                distribution[3]++; // Other
            }
        }

        return distribution;
    }

    private List<DashboardData.TopContributor> calculateTopContributors() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Transaction> transactions = transactionService.getTransactionsBetweenDates(
            sixMonthsAgo, LocalDateTime.now());

        return memberService.getActiveMembers().stream()
            .map(member -> {
                DashboardData.TopContributor contributor = new DashboardData.TopContributor();
                contributor.setName(member.getName());
                contributor.setPhone(member.getPhone());
                contributor.setStatus(member.getStatus().toString());

                List<Transaction> memberTransactions = transactions.stream()
                    .filter(t -> t.getMember() != null && t.getMember().getId().equals(member.getId()))
                    .toList();

                BigDecimal total = memberTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.CREDIT)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                contributor.setTotalContribution(total);

                memberTransactions.stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.CREDIT)
                    .max((t1, t2) -> t1.getDate().compareTo(t2.getDate()))
                    .ifPresent(t -> contributor.setLastPaymentDate(t.getDate()));

                return contributor;
            })
            .sorted((c1, c2) -> c2.getTotalContribution().compareTo(c1.getTotalContribution()))
            .limit(10)
            .toList();
    }

    private BigDecimal calculateMonthlyAverage() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<Transaction> transactions = transactionService.getTransactionsBetweenDates(
            sixMonthsAgo, LocalDateTime.now());

        BigDecimal totalIncome = transactions.stream()
            .filter(t -> t.getType() == Transaction.TransactionType.CREDIT)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalIncome.divide(BigDecimal.valueOf(6), 2, BigDecimal.ROUND_HALF_UP);
    }
} 