package at.pcgamingfreaks.service.tokenfreshing;

import at.pcgamingfreaks.model.enums.MediaSource;
import at.pcgamingfreaks.model.db.User;
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
	private final TraktTokenRefresher traktTokenRefresher;

	@Scheduled(cron = "0 ${random.int[0,59]} 0 * * *")
	public void refreshTokens() {
		List<User> users = userRepository.findAll();

		for (User user : users) {
			try {
				if (user.getConnections().get(MediaSource.TRAKT) != null && traktTokenRefresher.isValid())
					traktTokenRefresher.refresh(user);
			} catch (Exception e) {
				log.error("Failed to refresh trakt token for user {}", user.getUsername(), e);
			}
		}
		log.info("Refreshed tokens");
	}
}
