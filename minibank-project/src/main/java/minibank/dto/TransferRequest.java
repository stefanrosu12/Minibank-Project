package minibank.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank(message = "Source IBAN is required")
    private String sourceIban;

    @NotBlank(message = "Target IBAN is required")
    private String targetIban;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be strictly positive")
    private BigDecimal amount;

    private String idempotencyKey;
}
