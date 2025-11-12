package at.pcgamingfreaks.service.dataupdate.trakt;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.ContentType;
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
public class TraktMoviesUpdateService extends TraktUpdateService {
    private final ThirdPartyConfig thirdPartyConfig;

    @Override
    public ContentType getContentType() {
        return ContentType.MOVIES;
    }

    @Override
    public void updateData(long id, double score, User user) {
        if (!thirdPartyConfig.isTraktConfigValid())  throw new RuntimeException("Trakt config is invalid");

        try {
            new TraktV2(thirdPartyConfig.getTraktClientKey(), thirdPartyConfig.getTraktClientSecret(), thirdPartyConfig.getTraktRedirectUrl())
                    .accessToken(user.getTraktConnection().getAccessToken())
                    .sync()
                    .addRatings(new SyncItems().movies(new SyncMovie()
                            .id(MovieIds.trakt((int) id))
                            .rating(Rating.fromValue((int) score))))
                    .execute();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
