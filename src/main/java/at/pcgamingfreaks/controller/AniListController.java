package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.ThirdPartyConnection;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.AuthTokenResponseDTO;
import at.pcgamingfreaks.model.dto.ThirdPartyAuthRequestDTO;
import at.pcgamingfreaks.model.dto.ThirdPartyAuthResponseDTO;
import at.pcgamingfreaks.model.dto.ThirdPartyInfoResponseDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import at.pcgamingfreaks.model.util.JwtPayload;
import at.pcgamingfreaks.service.AnilistAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("anilist")
@CrossOrigin
@RequiredArgsConstructor
public class AniListController implements ThirdPartyController {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ThirdPartyConfig thirdPartyConfig;
    private final AnilistAuthService anilistAuthService;

    @PostMapping("auth/{username}")
    @PreAuthorize("authentication.principal.username == #username")
    @Validated
    public ResponseEntity<ThirdPartyAuthResponseDTO> auth(
            @PathVariable String username,
            @RequestBody ThirdPartyAuthRequestDTO request
    ) {
        log.info("Auth request for {}", username);

        if (!thirdPartyConfig.isAnilistConfigValid()) return ResponseEntity.badRequest().build();

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User does not exist"));
        if (user.getAnilistConnection() != null) throw new RuntimeException("Already authenticated");

        ThirdPartyAuthResponseDTO response = new ThirdPartyAuthResponseDTO();

        try {

            AuthTokenResponseDTO tokenResponse = anilistAuthService.auth(request.getCode());

            ThirdPartyConnection connection = new ThirdPartyConnection();
            connection.setService(ThirdPartyService.ANILIST);
            connection.setAccessToken(tokenResponse.getAccessToken());
            connection.setRefreshToken(tokenResponse.getRefreshToken());
            connection.setExpiresOn(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
            connection.setThirdpartyUserId(String.valueOf(extractUserIdFrom(connection.getAccessToken())));
            user.setAnilistConnection(connection);
            userRepository.save(user);
        } catch (Exception e) {
            log.error("", e);
            response.setMessage(e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("info")
    public ResponseEntity<ThirdPartyInfoResponseDTO> info() {
        if (thirdPartyConfig.getAnilistClientKey() == null) return ResponseEntity.notFound().build();
        ThirdPartyInfoResponseDTO response = new ThirdPartyInfoResponseDTO();
        response.setClientId(thirdPartyConfig.getAnilistClientKey());
        return ResponseEntity.ok(response);
    }

    private long extractUserIdFrom(String jwt) throws IOException {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] chunks = jwt.split("\\.");
        return objectMapper.readValue(decoder.decode(chunks[1]), JwtPayload.class).getUserId();
    }
}
