package at.pcgamingfreaks.service.dataupdate.anilist;

import at.pcgamingfreaks.model.ContentType;
import org.springframework.stereotype.Service;

@Service
public class AnilistAnimeUpdateService extends AnilistUpdateService {
    @Override
    public ContentType getContentType() {
        return ContentType.ANIME;
    }
}
