package at.pcgamingfreaks.service;

import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.*;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public LoginResponseDTO authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        String token = jwtService.create(user.getUsername());

        return new LoginResponseDTO(token);
    }

    public SignupResponseDTO signup(SignupRequestDTO request) {
        SignupResponseDTO response = new SignupResponseDTO();
        response.setUsernameTaken(userRepository.findByUsername(request.getUsername()).isPresent());
        response.setEmailTaken(userRepository.findByEmail(request.getEmail()).isPresent());

        if (!(response.isUsernameTaken() || response.isEmailTaken())) {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            response.setSignupSuccess(true);
        }

        return response;
    }

    public LoginResponseDTO refreshToken(String token) {
        if (jwtService.isTokenExpired(token)) throw new RuntimeException("Toke is invalid and can't be refreshed");

        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        String refreshedToken = jwtService.create(user.getUsername());

        return new LoginResponseDTO(refreshedToken);
    }

    public ChangePasswordResponseDTO changePassword(ChangePasswordRequestDTO request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if (user.isEmpty()) return new ChangePasswordResponseDTO(false, "Unknown user");

        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getOldPassword()));
            if (!auth.isAuthenticated()) return new ChangePasswordResponseDTO(false, "Invalid credentials");
            user.get().setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user.get());
            return new ChangePasswordResponseDTO(true, "");
        } catch (AuthenticationException e) {
            return new ChangePasswordResponseDTO(false, "Invalid credentials");
        }
    }

    public AccountDeletionResponseDTO deleteAccount(AccountDeletionRequestDTO request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if (user.isEmpty()) return new AccountDeletionResponseDTO(false, "Unknown user");

        try {
            userRepository.delete(user.get());
            log.info("Deleted {} successfully", user.get().getUsername());
            return new AccountDeletionResponseDTO(true, "");
        } catch (Exception ex) {
            return new AccountDeletionResponseDTO(false, "Deletion failed");
        }
    }
}
