package minibank.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor

public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false, unique = true)
    private String iban;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum AccountType {
        CHECKING, SAVINGS
    }
}
