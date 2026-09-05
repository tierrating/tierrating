package at.pcgamingfreaks.service;

import at.pcgamingfreaks.model.UserPrincipal;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
	private final Algorithm algorithm;
	private final long expirationMinutes;

	public JwtService(
			@Value("${security.jwt.secret-key}") String key,
			@Value("${security.jwt.expiration-minutes:60}") long expirationMinutes) {
		this.algorithm = Algorithm.HMAC256(key);
		this.expirationMinutes = expirationMinutes;
	}

	public String generateToken(UserPrincipal user) {
		return JWT.create()
				.withSubject(user.getUsername())
				.withClaim("user_id", user.getId())
				.withIssuedAt(Date.from(Instant.now()))
				.withExpiresAt(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))
				.sign(algorithm);
	}

	public UserPrincipal extractPrincipal(String token) {
		DecodedJWT jwt = JWT.require(algorithm).build().verify(token);
		Long id = jwt.getClaim("user_id").asLong();
		String username = jwt.getSubject();

		return new UserPrincipal(id, username);
	}

	public boolean isTokenValid(String token) {
		try {
			JWT.require(algorithm).build().verify(token);
			return true;
		} catch (JWTVerificationException e) {
			return false;
		}
	}
}
