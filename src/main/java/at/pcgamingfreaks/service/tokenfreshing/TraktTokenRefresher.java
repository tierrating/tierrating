package at.pcgamingfreaks.service.tokenfreshing;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.auth.ThirdPartyConnection;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.repo.UserRepository;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraktTokenRefresher implements TokenRefresher{
    private final UserRepository userRepository;
    private final ThirdPartyConfig thirdPartyConfig;

    @Override
    public boolean isValid() {
        return thirdPartyConfig.getTrakt().isValid();
    }

    @Override
    public void refresh(User user) {
        ThirdPartyConnection connection = user.getTraktConnection();
        if (connection.getExpiresOn().isBefore(LocalDateTime.now().plusDays(2))) {
            try {
                TraktV2 trakt = new TraktV2(thirdPartyConfig.getTrakt().getClient().getKey(), thirdPartyConfig.getTrakt().getClient().getSecret(), thirdPartyConfig.getTrakt().getRedirectUrl());
                Response<AccessToken> response = trakt.refreshAccessToken(connection.getRefreshToken());

                if (!response.isSuccessful()) throw new RuntimeException("Refreshing trakt access token failed");

                connection.setAccessToken(response.body().access_token);
                connection.setRefreshToken(response.body().refresh_token);
                connection.setExpiresOn(LocalDateTime.now().plusSeconds(response.body().expires_in));
                userRepository.save(user);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
