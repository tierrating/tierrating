package at.pcgamingfreaks.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class ThirdPartyConfig {

    @Value("${services.anilist.client.key}")
    private String anilistClientKey;
    @Value("${services.anilist.client.secret}")
    private String anilistClientSecret;
    @Value("${services.anilist.url}")
    private String anilistRedirectUrl;

    @Value("${services.trakt.client.key}")
    private String traktClientKey;
    @Value("${services.trakt.client.secret}")
    private String traktClientSecret;
    @Value("${services.trakt.url}")
    private String traktRedirectUrl;
}
