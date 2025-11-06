package at.pcgamingfreaks.service.dataupdate.anilist;

import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.service.dataupdate.DataUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import static at.pcgamingfreaks.config.GlobalProperties.ANILIST_API_URL;

@Slf4j
@Service
public abstract class AnilistUpdateService implements DataUpdateService {
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
        HttpGraphQlClient.create(WebClient.create(ANILIST_API_URL))
                .mutate()
                .header("Authorization", user.getAnilistConnection().getAccessToken())
                .build()
                .document(ANILIST_UPDATE_QUERY)
                .variable("mediaId", id)
                .variable("score", score)
                .retrieveSync("UpdateMediaListEntries");
    }
}
