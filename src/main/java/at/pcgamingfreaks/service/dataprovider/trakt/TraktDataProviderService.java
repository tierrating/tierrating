package at.pcgamingfreaks.service.dataprovider.trakt;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import at.pcgamingfreaks.service.dataprovider.DataProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class TraktDataProviderService implements DataProviderService {
    protected final UserRepository userRepository;
    protected final ThirdPartyConfig thirdPartyConfig;
    protected final ListEntryDtoMapper listEntryDtoMapper;

    @Override
    public ThirdPartyService getService() {
        return ThirdPartyService.TRAKT;
    }

    @Override
    public List<ListEntryDTO> fetchData(String username, ContentType type) {
        if (!thirdPartyConfig.isTraktConfigValid()) throw new RuntimeException("Trakt config is invalid");
        long duration = System.currentTimeMillis();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        try {
            return fetch(user)
                    .sorted(Comparator.comparing(ListEntryDTO::getScore).reversed())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            log.info("Fetched {} {} for {} in {}s",
                    getService(),
                    getContentType(),
                    username,
                    (System.currentTimeMillis() - duration) / 1000);
        }
    }

    protected Stream<ListEntryDTO> fetch(User user) throws IOException {
        Map<Long, ListEntryDTO> result = new HashMap<>();
        fetchRated(user).forEach(entry -> result.put(entry.getId(), entry));
        fetchWatched(user).forEach(entry -> {
            if (!result.containsKey(entry.getId())) result.put(entry.getId(), entry);
        });
        return result.values().stream();
    }

    abstract protected List<ListEntryDTO> fetchRated(User user) throws IOException;

    abstract protected List<ListEntryDTO> fetchWatched(User user) throws IOException;

}
