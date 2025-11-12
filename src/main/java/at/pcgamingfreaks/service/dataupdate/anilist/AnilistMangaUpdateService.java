package at.pcgamingfreaks.service.dataupdate.anilist;

import at.pcgamingfreaks.model.ContentType;
import org.springframework.stereotype.Service;

@Service
public class AnilistMangaUpdateService extends AnilistUpdateService {
    @Override
    public ContentType getContentType() {
        return ContentType.MANGA;
    }
}
