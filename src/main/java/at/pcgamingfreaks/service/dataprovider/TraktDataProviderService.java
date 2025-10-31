package at.pcgamingfreaks.service.dataprovider;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.BaseRatedEntity;
import com.uwetrottmann.trakt5.entities.UserSlug;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.enums.RatingsFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TraktDataProviderService implements DataProviderService {
    private final UserRepository userRepository;
    private final ThirdPartyConfig thirdPartyConfig;

    @Override
    public ThirdPartyService getService() {
        return ThirdPartyService.TRAKT;
    }

    @Override
    public List<ListEntryDTO> fetchData(String username, ContentType type) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        try {
            return switch (type) {
                case MOVIES -> fetchMovies(user);
                case TVSHOWS -> fetchTvShows(user);
                case TVSHOWS_SEASONS -> fetchTvShowsSeasons(user);
                default -> throw new RuntimeException("Invalid content type for provider");
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends BaseRatedEntity> List<ListEntryDTO> fetchData(User user, Response<List<T>> response) {
        if (!response.isSuccessful())
            throw new RuntimeException("Error retrieving watched movies of " + user.getUsername());


        return response.body().stream()
                .map(ListEntryDtoMapper::map)
                .sorted(Comparator.comparing(ListEntryDTO::getScore).reversed())
                .toList();
    }

    private List<ListEntryDTO> fetchMovies(User user) throws IOException {
        return fetchData(
                user,
                new TraktV2(thirdPartyConfig.getTraktClientKey(), thirdPartyConfig.getTraktClientSecret(), thirdPartyConfig.getTraktRedirectUrl())
                        .users().ratingsMovies(UserSlug.fromUsername(user.getTraktConnection().getThirdpartyUserId()), RatingsFilter.ALL, Extended.FULL).execute()
        );
    }

    private List<ListEntryDTO> fetchTvShows(User user) throws IOException{
        return fetchData(
                user,
                new TraktV2(thirdPartyConfig.getTraktClientKey(), thirdPartyConfig.getTraktClientSecret(), thirdPartyConfig.getTraktRedirectUrl())
                        .users().ratingsShows(UserSlug.fromUsername(user.getTraktConnection().getThirdpartyUserId()), RatingsFilter.ALL, Extended.FULL).execute()
        );
    }

    private List<ListEntryDTO> fetchTvShowsSeasons(User user) throws IOException {
        return fetchData(
                user,
                new TraktV2(thirdPartyConfig.getTraktClientKey(), thirdPartyConfig.getTraktClientSecret(), thirdPartyConfig.getTraktRedirectUrl())
                        .users().ratingsSeasons(UserSlug.fromUsername(user.getTraktConnection().getThirdpartyUserId()), RatingsFilter.ALL, Extended.FULL).execute()
        );
    }
}
