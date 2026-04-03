package minibank.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "exchange")
@Data

public class ExchangeRateProperties {
    private Map<String, BigDecimal> rates;
}
