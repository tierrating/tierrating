package at.pcgamingfreaks.service.dataprovider.trakt;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.BaseMovie;
import com.uwetrottmann.trakt5.entities.RatedMovie;
import com.uwetrottmann.trakt5.entities.UserSlug;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.enums.RatingsFilter;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.*;

@Service
public class TraktMovieDataProvider extends TraktDataProviderService{

    public TraktMovieDataProvider(UserRepository userRepository, ThirdPartyConfig thirdPartyConfig, ListEntryDtoMapper listEntryDtoMapper) {
        super(userRepository, thirdPartyConfig, listEntryDtoMapper);
    }

    @Override
    public ContentType getContentType() {
        return ContentType.MOVIES;
    }

    @Override
    protected List<ListEntryDTO> fetchRated(User user) throws IOException {
        Response<List<RatedMovie>> response = new TraktV2(
                thirdPartyConfig.getTrakt().getClient().getKey(),
                thirdPartyConfig.getTrakt().getClient().getSecret(),
                thirdPartyConfig.getTrakt().getRedirectUrl())
                .users()
                .ratingsMovies(
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
        Response<List<BaseMovie>> response = new TraktV2(
                thirdPartyConfig.getTrakt().getClient().getKey(),
                thirdPartyConfig.getTrakt().getClient().getSecret(),
                thirdPartyConfig.getTrakt().getRedirectUrl())
                .users()
                .watchedMovies(
                        UserSlug.fromUsername(user.getTraktConnection().getThirdpartyUserId()),
                        Extended.FULL)
                .execute();

        if (!response.isSuccessful())
            throw new RuntimeException("Error retrieving watched movies of " + user.getUsername());

        return response.body().stream()
                .map(listEntryDtoMapper::map)
                .toList();
    }
}
