package at.pcgamingfreaks.service.dataupdate.trakt;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.auth.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraktTvShowsSeasonsUpdateService extends TraktUpdateService{
    private final ThirdPartyConfig thirdPartyConfig;

    @Override
    public ContentType getContentType() {
        return ContentType.TVSHOWS_SEASONS;
    }

    @Override
    public void updateData(long id, double score, User user) {
        String body = "{\"seasons\":[{\"ids\":{\"trakt\":" + id + "},\"rating\":" + (int) score + "}]}";
        RestClient.builder()
                .baseUrl("https://api.trakt.tv")
                .defaultHeader("Authorization", user.getTraktConnection().getAccessToken())
                .build()
                .post()
                .uri("/sync/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .header("trakt-api-key", thirdPartyConfig.getTraktClientKey())
                .body(body)
                .retrieve()
                .body(String.class);
    }
}
