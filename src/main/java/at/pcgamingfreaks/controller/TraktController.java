package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.ThirdPartyConnection;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ThirdPartyAuthRequestDTO;
import at.pcgamingfreaks.model.dto.ThirdPartyAuthResponseDTO;
import at.pcgamingfreaks.model.repo.ThirdpartyConnectionRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import com.uwetrottmann.trakt5.entities.UserSlug;
import com.uwetrottmann.trakt5.enums.Extended;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("trakt")
@CrossOrigin
@RequiredArgsConstructor
public class TraktController {
    private final UserRepository userRepository;
    private final ThirdpartyConnectionRepository thirdpartyConnectionRepository;
    private final ThirdPartyConfig thirdPartyConfig;

    @PostMapping("auth/{username}")
    @PreAuthorize("authentication.principal.username == #username")
    @Validated
    public ResponseEntity<ThirdPartyAuthResponseDTO> auth(
            @PathVariable String username,
            @RequestBody ThirdPartyAuthRequestDTO requestBody
    ) {
        log.info("Auth request for {} with code {}", username, requestBody.getCode());

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User does not exist"));
        if (user.getAnilistConnection() != null) throw new RuntimeException("Already authenticated");

        ThirdPartyAuthResponseDTO responseDto = new ThirdPartyAuthResponseDTO();
        try {
            TraktV2 trakt = new TraktV2(thirdPartyConfig.getTraktClientKey(), thirdPartyConfig.getTraktClientSecret(), thirdPartyConfig.getTraktRedirectUrl());
            Response<AccessToken> response = trakt.exchangeCodeForAccessToken(requestBody.getCode());
            if (!response.isSuccessful()) throw new RuntimeException("Trakt OAuth responded with empty body");

            Response<com.uwetrottmann.trakt5.entities.User> traktUserInfo = trakt.accessToken(response.body().access_token).users().profile(UserSlug.ME, Extended.METADATA).execute();
            if (!traktUserInfo.isSuccessful()) throw new RuntimeException("Trakt OAuth responded with empty username");

            ThirdPartyConnection connection = user.getTraktConnection();
            connection.setService(ThirdPartyService.TRAKT);
            connection.setAccessToken(response.body().access_token);
            connection.setRefreshToken(response.body().refresh_token);
            connection.setExpiresOn(LocalDateTime.now().plusSeconds(response.body().expires_in));
            connection.setThirdpartyUserId(traktUserInfo.body().ids.slug);
            user.setTraktConnection(connection);
            userRepository.save(user);
        } catch (Exception e) {
            log.error(e.getMessage());
            responseDto.setMessage(e.getMessage());
        }
        return ResponseEntity.ok(responseDto);
    }
}
