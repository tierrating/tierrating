package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.dto.*;
import at.pcgamingfreaks.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        log.debug("Login request from {}", request.getUsername());
        return ResponseEntity.ok(authService.authenticate(request.getUsername(), request.getPassword()));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDTO> signup(@RequestBody SignupRequestDTO request) {
        log.debug("Signup request from {}", request.getUsername());
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestBody RefreshRequestDTO request) {
        return ResponseEntity.ok(authService.refreshToken(request.getToken()));
    }

    @PostMapping("/change-password")
    @PreAuthorize("authentication.principal.username == #request.username")
    public void changePassword(@RequestBody ChangePasswordRequestDTO request) {
        log.debug("Change password request from {}", request.getUsername());
        authService.changePassword(request);
    }

    @PostMapping("/delete-account")
    @PreAuthorize("authentication.principal.username == #request.username")
    public void deleteAccount(@RequestBody AccountDeletionRequestDTO request) {
        log.debug("Account deletion request from {}", request.getUsername());
        authService.deleteAccount(request);
    }
}