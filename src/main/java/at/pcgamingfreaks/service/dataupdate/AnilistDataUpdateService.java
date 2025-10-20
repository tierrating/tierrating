package at.pcgamingfreaks.service.dataupdate;

import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnilistDataUpdateService implements DataUpdateService {
    private final UserRepository userRepository;

    private final String ANILIST_API_URL = "https://graphql.anilist.co";
    private final String ANILIST_UPDATE_QUERY = """
            mutation ($listEntryId: Int, $mediaId: Int, $score: Float) {
              SaveMediaListEntry(id: $listEntryId, mediaId: $mediaId, score: $score) {
                id
                mediaId
                score
              }
            }
            """;

    @Override
    public ThirdPartyService getService() {
        return ThirdPartyService.ANILIST;
    }

    @Override
    public void updateData(long id, double score, User user) {

        createGraphQlClient()
                .mutate()
                .header("Authorization", user.getAnilistConnection().getAccessToken())
                .build()
                .document(ANILIST_UPDATE_QUERY)
                .variable("mediaId", id)
                .variable("score", score)
                .retrieveSync("UpdateMediaListEntries");

    }

    private HttpGraphQlClient createGraphQlClient() {
        WebClient webClient = WebClient.create(ANILIST_API_URL);
        return HttpGraphQlClient.create(webClient);
    }
}
