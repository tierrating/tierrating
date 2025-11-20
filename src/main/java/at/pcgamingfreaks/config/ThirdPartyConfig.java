package at.pcgamingfreaks.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "services")
public class ThirdPartyConfig {
    private ServiceConfig anilist;
    private ServiceConfig trakt;
}
