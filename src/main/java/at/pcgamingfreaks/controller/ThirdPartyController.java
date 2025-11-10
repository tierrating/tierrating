package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.dto.ThirdPartyAuthRequestDTO;
import at.pcgamingfreaks.model.dto.ThirdPartyAuthResponseDTO;
import at.pcgamingfreaks.model.dto.ThirdPartyInfoResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface ThirdPartyController {
    ResponseEntity<ThirdPartyAuthResponseDTO> auth(
            @PathVariable String username,
            @RequestBody ThirdPartyAuthRequestDTO request
    );

    ResponseEntity<ThirdPartyInfoResponseDTO> info();
}
