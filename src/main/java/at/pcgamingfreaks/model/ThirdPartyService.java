package at.pcgamingfreaks.model;

import at.pcgamingfreaks.model.auth.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ThirdPartyService {
    ANILIST(List.of(ContentType.ANIME, ContentType.MANGA)),
    TRAKT(List.of(ContentType.MOVIES, ContentType.TVSHOWS, ContentType.TVSHOWS_SEASONS));

    private final List<ContentType> allowedTypes;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ThirdPartyService from(String text) {
        for (ThirdPartyService service : ThirdPartyService.values()) {
            if (service.name().equalsIgnoreCase(text)) return service;
        }
        throw new IllegalArgumentException();
    }

    public static boolean hasUserConnection(User user, ThirdPartyService service) {
        return switch (service) {
            case ANILIST -> user.getAnilistConnection() != null;
            case TRAKT -> user.getTraktConnection() != null;
            default -> false;
        };
    }
}
