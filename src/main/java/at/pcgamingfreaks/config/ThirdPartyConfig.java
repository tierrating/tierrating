package at.pcgamingfreaks.config;

import graphql.com.google.common.base.Strings;
import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import static io.micrometer.common.util.StringUtils.isNotBlank;

@Getter
@Setter
@Configuration
public class ThirdPartyConfig {

    @Value("${services.anilist.client.key:#{null}}")
    private String anilistClientKey;
    @Value("${services.anilist.client.secret:#{null}}")
    private String anilistClientSecret;
    @Value("${services.anilist.url:#{null}}")
    private String anilistRedirectUrl;

    @Value("${services.trakt.client.key:#{null}}")
    private String traktClientKey;
    @Value("${services.trakt.client.secret:#{null}}")
    private String traktClientSecret;
    @Value("${services.trakt.url:#{null}}")
    private String traktRedirectUrl;

    public boolean isAnilistConfigValid() {
        return isNotBlank(anilistClientKey) && isNotBlank(anilistClientSecret) && isNotBlank(anilistRedirectUrl);
    }

    public boolean isTraktConfigValid() {
        return isNotBlank(traktClientKey) && isNotBlank(traktClientSecret) && isNotBlank(traktRedirectUrl);
    }
}
