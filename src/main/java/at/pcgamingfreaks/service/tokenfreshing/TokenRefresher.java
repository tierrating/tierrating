package at.pcgamingfreaks.service.tokenfreshing;

import at.pcgamingfreaks.model.auth.User;

public interface TokenRefresher {
    boolean isValid();
    void refresh(User user);
}
