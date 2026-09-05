package at.pcgamingfreaks.service;

import at.pcgamingfreaks.model.UserPrincipal;
import at.pcgamingfreaks.model.db.User;
import at.pcgamingfreaks.model.dto.LoginResponseDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	PasswordEncoder passwordEncoder;

	@Mock
	JwtService jwtService;

	@InjectMocks
	AuthService underTest;

	@Test
	void authenticate_unknownUser_throwsBadCredentials() {
		when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
		assertThrows(BadCredentialsException.class, () -> underTest.authenticate("ghost", "password"));
	}

	@Test
	void authenticate_wrongPassword_throwsBadCredentials() {
		User user = new User();
		user.setPassword("encoded");
		when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
		assertThrows(BadCredentialsException.class, () -> underTest.authenticate("test", "wrong"));
	}

	@Test
	void authenticate_validCredentials_returnsToken() {
		User user = new User();
		user.setId(1L);
		user.setUsername("test");
		user.setPassword("encoded");
		when(userRepository.findByUsername("test")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
		when(jwtService.generateToken(any())).thenReturn("token");

		LoginResponseDTO response = underTest.authenticate("test", "password");

		assertEquals("token", response.getToken());
	}

	@Test
	void refreshToken_invalidToken_throwsBadCredentials() {
		when(jwtService.isTokenValid("forged")).thenReturn(false);
		assertThrows(BadCredentialsException.class, () -> underTest.refreshToken("forged"));
	}

	@Test
	void refreshToken_validToken_returnsNewToken() {
		User user = new User();
		user.setId(1L);
		user.setUsername("test");
		when(jwtService.isTokenValid("valid")).thenReturn(true);
		when(jwtService.extractPrincipal("valid")).thenReturn(new UserPrincipal(1L, "test"));
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(jwtService.generateToken(any())).thenReturn("new-token");

		LoginResponseDTO response = underTest.refreshToken("valid");

		assertEquals("new-token", response.getToken());
	}
}
