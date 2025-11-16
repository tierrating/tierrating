package at.pcgamingfreaks.service.dataprovider.trakt;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.entities.RatedSeason;
import com.uwetrottmann.trakt5.entities.UserSlug;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.enums.RatingsFilter;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@Service
public class TraktTvShowSeasonsDataProvider extends TraktDataProviderService{

    public TraktTvShowSeasonsDataProvider(UserRepository userRepository, ThirdPartyConfig thirdPartyConfig, ListEntryDtoMapper listEntryDtoMapper) {
        super(userRepository, thirdPartyConfig, listEntryDtoMapper);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.TVSHOWS_SEASONS;
    }

    @Override
    protected List<ListEntryDTO> fetchRated(User user) throws IOException {
        Response<List<RatedSeason>> response = new TraktV2(
                        thirdPartyConfig.getTraktClientKey(),
                        thirdPartyConfig.getTraktClientSecret(),
                        thirdPartyConfig.getTraktRedirectUrl())
                .users()
                .ratingsSeasons(
                        UserSlug.fromUsername(user.getTraktConnection().getThirdpartyUserId()),
                        RatingsFilter.ALL,
                        Extended.FULL)
                .execute();

        if (!response.isSuccessful())
            throw new RuntimeException("Error retrieving watched movies of " + user.getUsername());

        return response.body().stream()
                .map(listEntryDtoMapper::map)
                .toList();
    }

    @Override
    protected List<ListEntryDTO> fetchWatched(User user) throws IOException {
        return List.of();
    }
}
