package minibank.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import minibank.config.ExchangeRateProperties;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {
    private final ExchangeRateProperties exchangeRates;

    @GetMapping
    public ResponseEntity<Map<String, BigDecimal>> getExchangeRates(){
        return ResponseEntity.ok(exchangeRates.getRates());
    }
}
