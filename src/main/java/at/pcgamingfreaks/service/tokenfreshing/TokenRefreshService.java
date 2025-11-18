package at.pcgamingfreaks.service.tokenfreshing;

import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshService {
    private final UserRepository userRepository;
    private final AnilistTokenRefresher anilistTokenRefresher;
    private final TraktTokenRefresher traktTokenRefresher;

    @Scheduled(cron = "* 59 22 * * *")
    public void refreshTokens() {
        List<User> users = userRepository.findAll();

        for (User user: users) {
            if (user.getTraktConnection() != null && traktTokenRefresher.isValid()) traktTokenRefresher.refresh(user);
            if (user.getAnilistConnection() != null && anilistTokenRefresher.isValid()) anilistTokenRefresher.refresh(user);
        }
        log.info("Refreshed tokens");
    }
}
