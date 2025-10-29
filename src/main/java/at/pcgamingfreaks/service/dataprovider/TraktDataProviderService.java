package at.pcgamingfreaks.service.dataprovider;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.RatedMovie;
import com.uwetrottmann.trakt5.entities.UserSlug;
import com.uwetrottmann.trakt5.enums.Extended;
import com.uwetrottmann.trakt5.enums.RatingsFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
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
            Response<List<RatedMovie>> response = new TraktV2(thirdPartyConfig.getTraktClientKey(), thirdPartyConfig.getTraktClientSecret(), thirdPartyConfig.getTraktRedirectUrl())
                    .users().ratingsMovies(UserSlug.fromUsername(user.getTraktConnection().getThirdpartyUserId()), RatingsFilter.ALL, Extended.FULL).execute();
            if (!response.isSuccessful()) throw new RuntimeException("Error retrieving watched movies of " + username);

            return response.body().stream()
                    .map(ListEntryDtoMapper::map)
                    .sorted((e1, e2) -> e1.getScore() < e2.getScore() ? 1 : -1)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
