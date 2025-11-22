package at.pcgamingfreaks.service.dataprovider.trakt;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.entities.RatedShow;
import com.uwetrottmann.trakt5.entities.UserSlug;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.enums.RatingsFilter;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

@Service
public class TraktTvShowDataProvider extends TraktDataProviderService {

    public TraktTvShowDataProvider(UserRepository userRepository, ThirdPartyConfig thirdPartyConfig, ListEntryDtoMapper listEntryDtoMapper) {
        super(userRepository, thirdPartyConfig, listEntryDtoMapper);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.TVSHOWS;
    }

    @Override
    protected List<ListEntryDTO> fetchRated(User user) throws IOException {
        Response<List<RatedShow>> response = new TraktV2(
                    thirdPartyConfig.getTrakt().getClient().getKey(),
                    thirdPartyConfig.getTrakt().getClient().getSecret(),
                    thirdPartyConfig.getTrakt().getRedirectUrl())
                .users()
                .ratingsShows(
                        UserSlug.fromUsername(user.getTraktConnection().getThirdpartyUserId()),
                        RatingsFilter.ALL,
                        Extended.NOSEASONS)
                .execute();

        if (!response.isSuccessful())
            throw new RuntimeException("Error retrieving watched movies of " + user.getUsername());

        return response.body().stream()
                .map(listEntryDtoMapper::map)
                .toList();
    }

    @Override
    protected List<ListEntryDTO> fetchWatched(User user) throws IOException {
        Response<List<BaseShow>> response = new TraktV2(
                    thirdPartyConfig.getTrakt().getClient().getKey(),
                    thirdPartyConfig.getTrakt().getClient().getSecret(),
                    thirdPartyConfig.getTrakt().getRedirectUrl())
                .users()
                .watchedShows(
                        UserSlug.fromUsername(user.getTraktConnection().getThirdpartyUserId()),
                        Extended.NOSEASONS)
                .execute();

        if (!response.isSuccessful())
            throw new RuntimeException("Error retrieving watched movies of " + user.getUsername());

        return response.body().stream()
                .map(listEntryDtoMapper::map)
                .toList();
    }
}
