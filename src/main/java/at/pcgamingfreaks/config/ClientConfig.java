package at.pcgamingfreaks.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

import static io.micrometer.common.util.StringUtils.isNotBlank;

@Getter
@Setter
@Configuration
public class ClientConfig {
    private String key;
    private String secret;

    public boolean isValid() {
        return isNotBlank(key) && isNotBlank(secret);
    }
}
