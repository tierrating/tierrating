package at.pcgamingfreaks.service.dataupdate;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.User;

public interface DataUpdateService {
    ThirdPartyService getService();
    ContentType getContentType();
    void updateData(long id, double score, User user);
}
