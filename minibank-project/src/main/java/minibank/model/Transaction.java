package minibank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor

public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    private String counterpartyIban;

    @Column(nullable = false)
    private Long transferId;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    }
}
