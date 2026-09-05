package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.mapper.UserDtoMapper;
import at.pcgamingfreaks.model.enums.MediaSource;
import at.pcgamingfreaks.model.db.MediaSourceConnection;
import at.pcgamingfreaks.model.db.User;
import at.pcgamingfreaks.model.dto.UserDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
	private final UserRepository userRepository;

	@GetMapping("{username}")
	public ResponseEntity<UserDTO> user(@PathVariable String username) {
		return userRepository.findByUsername(username)
				.map(value -> ResponseEntity.ok(UserDtoMapper.map(value)))
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@DeleteMapping("{username}/remove/{service}")
	@PreAuthorize("authentication.principal.username == #username")
	public void removeThirdPartyService(@PathVariable String username, @PathVariable MediaSource service) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
		MediaSourceConnection connection = user.getConnections().get(service);
		if (connection == null) return; // idempotent: nothing to remove

		user.getConnections().remove(service); // triggers orphan removal on save
		userRepository.save(user);
	}

}
