package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.mapper.UserDtoMapper;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ThirdPartyRemovalResponseDTO;
import at.pcgamingfreaks.model.dto.UserDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import at.pcgamingfreaks.service.thirdpartyconnector.ThirdPartyConnectorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;
    private final ThirdPartyConnectorFactory thirdPartyConnectorFactory;

    @GetMapping("{username}")
    public ResponseEntity<UserDTO> user(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(value -> ResponseEntity.ok(UserDtoMapper.map(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("{username}/remove/{service}")
    @PreAuthorize("authentication.principal.username == #username")
    public ResponseEntity<ThirdPartyRemovalResponseDTO> removeThirdPartyService(@PathVariable String username, @PathVariable ThirdPartyService service) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) return ResponseEntity.ok(new ThirdPartyRemovalResponseDTO(false, "Unknown user"));

        return ResponseEntity.ok(thirdPartyConnectorFactory.getProvider(service).removeConnection(user.get()));
    }
}
