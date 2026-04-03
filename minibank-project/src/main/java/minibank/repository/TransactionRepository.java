package minibank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import minibank.model.Transaction;

import java.time.Instant;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccountIdOrderByTimestampAsc(Long accountId, Pageable pageable);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.account.id = :accountId
            AND t.type IN ('TRANSFER_OUT', 'WITHDRAWAL')
            AND t.timestamp >= :startOfDay
            AND t.timestamp < :endOfDay
            """)
    List<Transaction> findOutgoingTransactionsForDay(
            @Param("accountId") Long accountId,
            @Param("startOfDay")Instant startOfDay,
            @Param("endOfDay") Instant endOfDay
            );
}
