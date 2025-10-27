package at.pcgamingfreaks.model;

import at.pcgamingfreaks.model.auth.User;
import com.fasterxml.jackson.annotation.JsonCreator;

public enum ThirdPartyService {
    ANILIST,
    TRAKT;

    public String toString() {
        return name().toLowerCase();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ThirdPartyService from(String text) {
        for (ThirdPartyService service : ThirdPartyService.values()) {
            if (service.toString().equalsIgnoreCase(text)) return service;
        }
        throw new IllegalArgumentException();
    }

    public static boolean hasUserConnection(User user, ThirdPartyService service) {
        return switch (service) {
            case ANILIST -> user.getAnilistConnection() != null;
            default ->  false;
        };
    }
}
