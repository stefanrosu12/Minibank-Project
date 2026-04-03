package minibank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transfers")
@Data
@NoArgsConstructor

public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceIban;

    @Column(nullable = false)
    private String targetIban;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String targetCurrency;

    @Column(precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column(precision = 19, scale = 2)
    private BigDecimal convertedAmount;

    @Column(unique = true)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
