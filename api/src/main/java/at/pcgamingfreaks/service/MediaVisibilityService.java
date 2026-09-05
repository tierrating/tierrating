package at.pcgamingfreaks.service;

import at.pcgamingfreaks.model.db.MediaSourceConnection;
import at.pcgamingfreaks.model.db.MediaTypeSettings;
import at.pcgamingfreaks.model.db.User;
import at.pcgamingfreaks.model.enums.MediaSource;
import at.pcgamingfreaks.model.enums.MediaType;
import at.pcgamingfreaks.model.enums.MediaState;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves per media type visibility settings for a user.
 * Owners always see their own data. For other users the media type settings of the
 * connection decide: media types without explicit settings are public by default.
 */
@Service
public class MediaVisibilityService {

	public boolean isViewableBy(User owner, String requesterUsername, MediaSource source, MediaType type) {
		if (requesterUsername != null && requesterUsername.equals(owner.getUsername())) return true;

		MediaSourceConnection connection = owner.getConnections().get(source);
		if (connection == null) return false;

		MediaTypeSettings settings = connection.getMediaTypeSettings().get(type);
		if (settings == null) return true;

		return settings.isShowPublic() && !settings.isHidden();
	}

	/**
	 * @return states that must not be shown to the given requester, empty set for the owner
	 */
	public Set<MediaState> hiddenStatesFor(User owner, String requesterUsername, MediaSource source, MediaType type) {
		if (requesterUsername != null && requesterUsername.equals(owner.getUsername())) return Set.of();

		MediaSourceConnection connection = owner.getConnections().get(source);
		if (connection == null) return Set.of();

		MediaTypeSettings settings = connection.getMediaTypeSettings().get(type);
		if (settings == null || settings.getHiddenStates() == null) return Set.of();

		return settings.getHiddenStates().stream().collect(Collectors.toUnmodifiableSet());
	}
}
