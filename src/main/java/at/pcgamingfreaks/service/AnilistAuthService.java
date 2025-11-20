package at.pcgamingfreaks.service;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.dto.AuthTokenResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnilistAuthService {
    private final ThirdPartyConfig thirdPartyConfig;

    public AuthTokenResponseDTO auth(String code) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("grant_type", "authorization_code");
        requestBody.put("client_id", thirdPartyConfig.getAnilist().getClient().getKey());
        requestBody.put("client_secret", thirdPartyConfig.getAnilist().getClient().getSecret());
        requestBody.put("redirect_uri", thirdPartyConfig.getAnilist().getRedirectUrl());
        requestBody.put("code", code);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<AuthTokenResponseDTO> tokenResponse = restTemplate.exchange(
                "https://anilist.co/api/v2/oauth/token",
                HttpMethod.POST,
                entity,
                AuthTokenResponseDTO.class
        );

        if (!tokenResponse.hasBody() || tokenResponse.getBody() == null)
            throw new RuntimeException("AniList OAuth responded with empty body");

        return tokenResponse.getBody();
    }
}
