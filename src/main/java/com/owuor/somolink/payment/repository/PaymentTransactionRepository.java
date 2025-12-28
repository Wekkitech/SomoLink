package com.owuor.somolink.payment.repository;

import com.owuor.somolink.payment.dto.PaymentTransactionAnalyticsDto;
import com.owuor.somolink.payment.dto.paymentTransactionResponseDto;
import com.owuor.somolink.payment.entity.PaymentTransaction;
import com.owuor.somolink.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, Long> {

    // ✅ Used to avoid duplicate STK callbacks
    boolean existsByCheckoutRequestId(String checkoutRequestId);

    // ✅ Used when callback sends receipt number
    Optional<PaymentTransaction> findByMpesaReceiptNumber(String mpesaReceiptNumber);

    // ✅ Search + filter + pagination
    @Query("""
                SELECT new com.owuor.somolink.payment.dto.paymentTransactionResponseDto(
                pt.id, pt.profileName, pt.phoneNumber, pt.amount,
                pt.mpesaReceiptNumber, pt.status, pt.paidAt
            )
                FROM PaymentTransaction pt
                WHERE (:status IS NULL OR pt.status = :status)
                  AND (
                       :search IS NULL
                       OR pt.mpesaReceiptNumber LIKE CONCAT('%', CAST(:search AS string), '%')
                       OR pt.phoneNumber LIKE CONCAT('%', CAST(:search AS string), '%')
                       OR pt.profileName LIKE CONCAT('%', CAST(:search AS string), '%')
                  )
                ORDER BY pt.paidAt DESC
            """)
    Page<paymentTransactionResponseDto> searchTransactions(
            @Param("search") String search,
            @Param("status") PaymentStatus status,
            Pageable pageable
    );
    long countByStatus(PaymentStatus status);

    // Sum of amounts by status
    @Query("""
        SELECT new com.owuor.somolink.payment.dto.PaymentTransactionAnalyticsDto(
            SUM(p.amount)
        )
        FROM PaymentTransaction p
        WHERE p.status = :status
    """)
    PaymentTransactionAnalyticsDto sumAmountByStatus(@Param("status") PaymentStatus status);

    // Sum of amounts by status for a specific date
    @Query("""
        SELECT new com.owuor.somolink.payment.dto.PaymentTransactionAnalyticsDto(
            SUM(p.amount)
        )
        FROM PaymentTransaction p
        WHERE p.status = :status AND FUNCTION('DATE', p.paidAt) = :date
    """)
    PaymentTransactionAnalyticsDto sumAmountByStatusAndPaidAtDate(
            @Param("status") PaymentStatus status,
            @Param("date") LocalDate date
    );

    // Find transactions by status and date range, selecting only needed fields
    @Query("""
        SELECT new com.owuor.somolink.payment.dto.paymentTransactionResponseDto(
            p.id,
            p.profileName,
            p.phoneNumber,
            p.amount,
            p.mpesaReceiptNumber,
            p.status,
            p.paidAt
        )
        FROM PaymentTransaction p
        WHERE p.status = :status AND p.paidAt BETWEEN :start AND :end
        ORDER BY p.paidAt ASC
    """)
    List<paymentTransactionResponseDto> findByStatusAndPaidAtBetween(
            @Param("status") PaymentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );}
