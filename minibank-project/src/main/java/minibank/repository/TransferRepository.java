package minibank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import minibank.model.Transfer;
import java.time.Instant;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            SELECT t FROM Transfer t
            WHERE (:iban IS NULL OR t.sourceIban = :iban OR t.targetIban = :iban)
            AND (:from IS NULL OR t.createdAt >= :from)
            AND (:to IS NULL OR t.createdAt <= :to)
            """)
    Page<Transfer> findWithFilters(
            @Param("iban") String iban,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );
}
