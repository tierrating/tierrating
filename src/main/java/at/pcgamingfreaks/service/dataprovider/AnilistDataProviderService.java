package at.pcgamingfreaks.service.dataprovider;

import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.anilist.AniListListEntry;
import at.pcgamingfreaks.model.anilist.AniListPage;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnilistDataProviderService implements DataProviderService {
    private final UserRepository userRepository;
    private final String ANILIST_API_URL = "https://graphql.anilist.co";

    public ThirdPartyService getService() {
        return ThirdPartyService.ANILIST;
    }

    @Override
    public List<ListEntryDTO> fetchData(String username, ContentType type) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        String query = """
                query ($userId: Int, $type: MediaType, $status: MediaListStatus, $page: Int, $perPage: Int) {
                    Page(page: $page, perPage: $perPage) {
                        pageInfo {
                            currentPage
                            hasNextPage
                            perPage
                        }
                        mediaList(userId: $userId, type: $type, status: $status) {
                            score(format: POINT_10_DECIMAL)
                            media {
                                id
                                title {
                                    romaji
                                    english
                                    native
                                }
                                coverImage {
                                    large
                                    extraLarge
                                }
                            }
                        }
                    }
                }
                """;

        List<AniListListEntry> result = new ArrayList<>();

        long timerStart = System.currentTimeMillis();
        AniListPage page;
        int currentPage = 1;
        do {
            page = createGraphQlClient()
                    .document(query)
                    .variable("userId", user.getAnilistConnection().getAnilistId())
                    .variable("type", type.name())
                    .variable("status", "COMPLETED")
                    .variable("page", currentPage++)
                    .variable("perPage", 50)
                    .retrieveSync("Page")
                    .toEntity(AniListPage.class);
            result.addAll(page.getMediaList());
        } while (page.getPageInfo().isHasNextPage());
        log.info("Getting data in {}ms", System.currentTimeMillis() - timerStart);

        return result.stream()
                .map(ListEntryDtoMapper::map)
                .sorted(Comparator.comparing(ListEntryDTO::getScore).reversed())
                .toList();
    }

    private HttpGraphQlClient createGraphQlClient() {
        WebClient webClient = WebClient.create(ANILIST_API_URL);
        return HttpGraphQlClient.create(webClient);
    }
}
