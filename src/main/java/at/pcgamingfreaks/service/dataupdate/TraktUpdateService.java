package at.pcgamingfreaks.service.dataupdate;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.User;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.*;
import com.uwetrottmann.trakt5.enums.Rating;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraktUpdateService implements DataUpdateService {
    private final ThirdPartyConfig thirdPartyConfig;

    @Override
    public ThirdPartyService getService() {
        return ThirdPartyService.TRAKT;
    }

    @Override
    public void updateData(long id, double score, User user, ContentType contentType) {
        try {
            switch (contentType) {
                case MOVIES ->
                        update(new SyncMovie().id(MovieIds.trakt((int) id)).rating(Rating.fromValue((int) score)), user);
                case TVSHOWS ->
                        update(new SyncShow().id(ShowIds.trakt((int) id)).rating(Rating.fromValue((int) score)), user);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void update(SyncMovie movie, User user) throws IOException {
        new TraktV2(thirdPartyConfig.getTraktClientKey(), thirdPartyConfig.getTraktClientSecret(), thirdPartyConfig.getTraktRedirectUrl())
                .accessToken(user.getTraktConnection().getAccessToken())
                .sync().addRatings(new SyncItems().movies(movie)).execute();
    }

    private void update(SyncShow show, User user) throws IOException {
        new TraktV2(thirdPartyConfig.getTraktClientKey(), thirdPartyConfig.getTraktClientSecret(), thirdPartyConfig.getTraktRedirectUrl())
                .accessToken(user.getTraktConnection().getAccessToken())
                .sync().addRatings(new SyncItems().shows(show)).execute();
    }
}
