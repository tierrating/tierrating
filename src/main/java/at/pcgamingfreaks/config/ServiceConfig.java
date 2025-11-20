package at.pcgamingfreaks.config;

import lombok.Getter;
import lombok.Setter;

import static io.micrometer.common.util.StringUtils.isNotBlank;

@Getter
@Setter
public class ServiceConfig {
    private ClientConfig client;
    private String redirectUrl;

    public boolean isValid() {
        return client.isValid() && isNotBlank(redirectUrl);
    }

    public void setUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
