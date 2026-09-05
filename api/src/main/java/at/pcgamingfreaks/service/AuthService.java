package at.pcgamingfreaks.service;

import at.pcgamingfreaks.model.UserPrincipal;
import at.pcgamingfreaks.model.db.User;
import at.pcgamingfreaks.model.dto.ChangePasswordRequestDTO;
import at.pcgamingfreaks.model.dto.LoginResponseDTO;
import at.pcgamingfreaks.model.dto.SignupRequestDTO;
import at.pcgamingfreaks.model.dto.SignupResponseDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public LoginResponseDTO authenticate(String username, String password) {
		User user;
		try {
			user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
		} catch (UsernameNotFoundException e) {
			// do not leak whether the username exists on login
			throw new BadCredentialsException("Invalid credentials");
		}
		if (!passwordEncoder.matches(password, user.getPassword()))
			throw new BadCredentialsException("Invalid credentials");
		String token = jwtService.generateToken(new UserPrincipal(user.getId(), user.getUsername()));
		return new LoginResponseDTO(token);
	}

	@Transactional
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
		if (!jwtService.isTokenValid(token)) throw new BadCredentialsException("Invalid token");

		UserPrincipal userPrincipal = jwtService.extractPrincipal(token);
		User user = userRepository.findById(userPrincipal.getId())
				.orElseThrow(() -> new UsernameNotFoundException(userPrincipal.getUsername()));

		String refreshedToken = jwtService.generateToken(new UserPrincipal(user.getId(), user.getUsername()));

		return new LoginResponseDTO(refreshedToken);
	}

	@Transactional
	public void changePassword(ChangePasswordRequestDTO request) {
		User user = userRepository.findByUsername(request.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException(request.getUsername()));

		if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) throw new BadCredentialsException("Invalid credentials");
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
	}

	@Transactional
	public void deleteAccount(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException(username));
		userRepository.delete(user);
		log.info("Deleted {} successfully", user.getUsername());
	}

}
