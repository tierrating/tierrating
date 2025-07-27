package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.mapper.ListEntryDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.anilist.AniListListEntry;
import at.pcgamingfreaks.model.anilist.AniListPage;
import at.pcgamingfreaks.model.auth.AniListConnection;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.*;
import at.pcgamingfreaks.model.repo.AniListConnectionRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import at.pcgamingfreaks.model.util.JwtObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RestController
@RequestMapping("anilist")
@CrossOrigin
@RequiredArgsConstructor
public class AniListController {
    private final String ANILIST_API_URL = "https://graphql.anilist.co";
    private final UserRepository userRepository;
    private final AniListConnectionRepository aniListConnectionRepository;
    private final ObjectMapper objectMapper;

    @Value("${services.anilist.client.key}")
    private String clientKey;
    @Value("${services.anilist.client.secret}")
    private String clientSecret;

    @GetMapping("{username}/{type}")
    public List<ListEntryDTO> getData(@PathVariable String username, @PathVariable ContentType type) {
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
                            score(format: POINT_10)
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

    @PostMapping("auth/{username}")
    public ResponseEntity<ThirdPartyAuthResponseDTO> auth(
            @PathVariable String username,
            @RequestBody ThirdPartyAuthRequestDTO request
    ) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "authorization_code");
        requestBody.put("client_id", clientKey);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("redirect_uri", "http://localhost:3001/auth/anilist");
        requestBody.put("code", request.getCode());

        RestTemplate restTemplate = new RestTemplate();
        ThirdPartyAuthResponseDTO response = new ThirdPartyAuthResponseDTO();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<AniListAuthTokenResponseDTO> tokenResponse = restTemplate.exchange(
                    "https://anilist.co/api/v2/oauth/token",
                    HttpMethod.POST,
                    entity,
                    AniListAuthTokenResponseDTO.class
            );

            if (!tokenResponse.hasBody() || tokenResponse.getBody() == null)
                throw new RuntimeException("AniList OAuth responded with empyt body");


            User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User does not exist"));
            user.setAnilistConnection(user.getAnilistConnection() != null ? user.getAnilistConnection() : new AniListConnection());

            AniListConnection connection = user.getAnilistConnection();
            connection.setUser(user);
            connection.setAccessToken(tokenResponse.getBody().getAccessToken());
            connection.setRefreshToken(tokenResponse.getBody().getRefreshToken());
            connection.setExpiresOn(LocalDateTime.now().plusSeconds(tokenResponse.getBody().getExpiresIn()));
            connection.setAnilistId(extractUserIdFrom(connection.getAccessToken()));
            aniListConnectionRepository.save(connection);
            userRepository.save(user);

        } catch (Exception e) {
            log.error("", e);
            response.setMessage(e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    private long extractUserIdFrom(String jwt) throws IOException {
        Base64.Decoder decoder = Base64.getDecoder();
        return objectMapper.readValue(decoder.decode(jwt), JwtObject.class).getPayload().getUserId();
    }
}
