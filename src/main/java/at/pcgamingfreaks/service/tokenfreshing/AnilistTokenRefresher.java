package at.pcgamingfreaks.service.tokenfreshing;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.auth.ThirdPartyConnection;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.AuthTokenResponseDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import at.pcgamingfreaks.service.AnilistAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnilistTokenRefresher implements TokenRefresher {
    private final UserRepository userRepository;
    private final AnilistAuthService anilistAuthService;
    private final ThirdPartyConfig thirdPartyConfig;

    @Override
    public boolean isValid() {
        return thirdPartyConfig.isAnilistConfigValid();
    }

    @Override
    public void refresh(User user) {
        ThirdPartyConnection connection = user.getAnilistConnection();
        if (connection.getExpiresOn().isBefore(LocalDateTime.now().plusYears(1))) {
            AuthTokenResponseDTO tokenResponse = anilistAuthService.auth(connection.getRefreshToken());
            connection.setAccessToken(tokenResponse.getAccessToken());
            connection.setRefreshToken(tokenResponse.getRefreshToken());
            connection.setExpiresOn(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
            userRepository.save(user);
        }
    }
}
