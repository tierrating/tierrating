package at.pcgamingfreaks.service.dataprovider.anilist;

import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.anilist.AniListListEntry;
import at.pcgamingfreaks.model.anilist.AniListPage;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import at.pcgamingfreaks.service.dataprovider.DataProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static at.pcgamingfreaks.config.GlobalProperties.ANILIST_API_URL;

@Slf4j
@Service
@RequiredArgsConstructor
public abstract class AnilistDataProviderService implements DataProviderService {
    private final UserRepository userRepository;
    private final ListEntryDtoMapper listEntryDtoMapper;

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

        long duration = System.currentTimeMillis();
        AniListPage page;
        int currentPage = 1;
        do {
            page = HttpGraphQlClient.create(WebClient.create(ANILIST_API_URL))
                    .document(query)
                    .variable("userId", user.getAnilistConnection().getThirdpartyUserId())
                    .variable("type", type.name())
                    .variable("status", "COMPLETED")
                    .variable("page", currentPage++)
                    .variable("perPage", 50)
                    .retrieveSync("Page")
                    .toEntity(AniListPage.class);
            result.addAll(page.getMediaList());
        } while (page.getPageInfo().isHasNextPage());

        log.info("Fetched {} {} for {} in {}s",
                getService(),
                getContentType(),
                username,
                (System.currentTimeMillis() - duration) / 1000);

        return result.stream()
                .map(listEntryDtoMapper::map)
                .sorted(Comparator.comparing(ListEntryDTO::getScore).reversed())
                .toList();
    }
}
