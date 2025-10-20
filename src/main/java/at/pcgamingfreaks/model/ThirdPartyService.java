package at.pcgamingfreaks.model;

import at.pcgamingfreaks.model.auth.User;

public enum ThirdPartyService {
    ANILIST,
    TRAKT;

    public static boolean hasUserConnection(User user, ThirdPartyService service) {
        return switch (service) {
            case ANILIST -> user.getAnilistConnection() != null;
            default ->  false;
        };
    }
}
