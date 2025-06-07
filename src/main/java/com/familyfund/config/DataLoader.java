package com.familyfund.config;

import com.familyfund.model.Member;
import com.familyfund.model.Subscription;
import com.familyfund.model.Transaction;
import com.familyfund.service.MemberService;
import com.familyfund.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Profile("dev")
public class DataLoader {

    private final MemberService memberService;
    private final SubscriptionService subscriptionService;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            // Create sample members
            Member member1 = new Member();
            member1.setName("John Doe");
            member1.setEmail("john.doe@example.com");
            member1.setPhone("(555) 123-4567");
            member1 = memberService.createMember(member1);

            Member member2 = new Member();
            member2.setName("Jane Smith");
            member2.setEmail("jane.smith@example.com");
            member2.setPhone("(555) 987-6543");
            member2 = memberService.createMember(member2);

            Member member3 = new Member();
            member3.setName("Bob Johnson");
            member3.setEmail("bob.johnson@example.com");
            member3.setPhone("(555) 456-7890");
            member3 = memberService.createMember(member3);

            // Create sample subscriptions and payments
            YearMonth currentMonth = YearMonth.now();
            YearMonth lastMonth = currentMonth.minusMonths(1);

            List<Member> members = List.of(member1, member2, member3);
            BigDecimal monthlyFee = new BigDecimal("50.00");

            for (Member member : members) {
                // Last month - all paid
                subscriptionService.recordPayment(member.getId(), lastMonth, monthlyFee);

                // Current month - some paid, some pending
                if (members.indexOf(member) < 2) {
                    subscriptionService.recordPayment(member.getId(), currentMonth, monthlyFee);
                }
            }
        };
    }
} 