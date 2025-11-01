package at.pcgamingfreaks.service;

import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.LoginResponseDTO;
import at.pcgamingfreaks.model.dto.SignupRequestDTO;
import at.pcgamingfreaks.model.dto.SignupResponseDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}
