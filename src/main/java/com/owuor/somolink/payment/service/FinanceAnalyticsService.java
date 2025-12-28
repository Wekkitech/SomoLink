package com.owuor.somolink.payment.service;

import com.owuor.somolink.payment.dto.FinanceAnalyticsDto;
import com.owuor.somolink.payment.dto.RevenueGraphDto;
import com.owuor.somolink.payment.dto.paymentTransactionResponseDto;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.enums.PaymentStatus;
import com.owuor.somolink.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinanceAnalyticsService {

    private final PaymentTransactionRepository transactionRepository;

    public FinanceAnalyticsDto getAnalytics() {
        BigDecimal totalRevenue = transactionRepository.sumAmountByStatus(PaymentStatus.PAID).getSumAmount();
        BigDecimal todaysRevenue = transactionRepository.sumAmountByStatusAndPaidAtDate(PaymentStatus.PAID, LocalDate.now()).getSumAmount();

        long totalTransactions = transactionRepository.count();
        long successfulTransactions = transactionRepository.countByStatus(PaymentStatus.PAID);
        long failedTransactions = transactionRepository.countByStatus(PaymentStatus.FAILED);

        return new FinanceAnalyticsDto(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                todaysRevenue != null ? todaysRevenue : BigDecimal.ZERO,
                totalTransactions,
                successfulTransactions,
                failedTransactions
        );
    }
    public List<RevenueGraphDto> getYearlyRevenue(int year) {
        List<paymentTransactionResponseDto> transactions = transactionRepository.findByStatusAndPaidAtBetween(
                PaymentStatus.PAID,
                LocalDateTime.of(year, 1, 1, 0, 0),
                LocalDateTime.of(year, 12, 31, 23, 59)
        );

        Map<Integer, BigDecimal> revenueByMonth = new TreeMap<>();
        for (int m = 1; m <= 12; m++) revenueByMonth.put(m, BigDecimal.ZERO);

        for (paymentTransactionResponseDto tx : transactions) {
            int month = tx.getPaidAt().getMonthValue();
            revenueByMonth.put(month, revenueByMonth.get(month).add(tx.getAmount()));
        }

        return revenueByMonth.entrySet().stream()
                .map(e -> new RevenueGraphDto(
                        Month.of(e.getKey()).name(),
                        e.getValue()
                ))
                .collect(Collectors.toList());
    }

    public List<RevenueGraphDto> getMonthlyRevenue(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        List<paymentTransactionResponseDto> transactions = transactionRepository.findByStatusAndPaidAtBetween(
                PaymentStatus.PAID,
                ym.atDay(1).atStartOfDay(),
                ym.atEndOfMonth().atTime(23, 59)
        );

        Map<Integer, BigDecimal> revenueByDay = new TreeMap<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) revenueByDay.put(d, BigDecimal.ZERO);

        for (paymentTransactionResponseDto tx : transactions) {
            int day = tx.getPaidAt().getDayOfMonth();
            revenueByDay.put(day, revenueByDay.get(day).add(tx.getAmount()));
        }

        return revenueByDay.entrySet().stream()
                .map(e -> new RevenueGraphDto(
                        String.valueOf(e.getKey()),
                        e.getValue()
                ))
                .collect(Collectors.toList());
    }
}
