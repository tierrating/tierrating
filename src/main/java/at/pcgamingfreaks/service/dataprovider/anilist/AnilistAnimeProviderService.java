package at.pcgamingfreaks.service.dataprovider.anilist;

import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.repo.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AnilistAnimeProviderService extends AnilistDataProviderService{

    public AnilistAnimeProviderService(UserRepository userRepository, ListEntryDtoMapper listEntryDtoMapper) {
        super(userRepository, listEntryDtoMapper);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.ANIME;
    }
}