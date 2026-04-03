package minibank.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountCreateRequest {
    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "IBAN is required")
    private String iban;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Account type is required")
    private String accountType;
}
