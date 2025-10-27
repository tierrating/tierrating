package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.auth.AniListConnection;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.AniListAuthTokenResponseDTO;
import at.pcgamingfreaks.model.dto.ThirdPartyAuthRequestDTO;
import at.pcgamingfreaks.model.dto.ThirdPartyAuthResponseDTO;
import at.pcgamingfreaks.model.repo.AniListConnectionRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import at.pcgamingfreaks.model.util.JwtPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("anilist")
@CrossOrigin
@RequiredArgsConstructor
public class AniListController {
    private final UserRepository userRepository;
    private final AniListConnectionRepository aniListConnectionRepository;
    private final ObjectMapper objectMapper;

    @Value("${services.anilist.client.key}")
    private String clientKey;
    @Value("${services.anilist.client.secret}")
    private String clientSecret;

    @PostMapping("auth/{username}")
    @PreAuthorize("authentication.principal.username == #username")
    @Validated
    public ResponseEntity<ThirdPartyAuthResponseDTO> auth(
            @PathVariable String username,
            @RequestBody ThirdPartyAuthRequestDTO request
    ) {
        log.info("Auth request for {}", username);
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "authorization_code");
        requestBody.put("client_id", clientKey);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("redirect_uri", "http://localhost:3000/auth/anilist");
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
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] chunks = jwt.split("\\.");
        return objectMapper.readValue(decoder.decode(chunks[1]), JwtPayload.class).getUserId();
    }
}
