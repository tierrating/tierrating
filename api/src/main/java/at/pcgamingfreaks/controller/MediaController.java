package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.UserPrincipal;
import at.pcgamingfreaks.model.dto.MediaEntryDTO;
import at.pcgamingfreaks.model.enums.MediaType;
import at.pcgamingfreaks.model.enums.MediaSource;
import at.pcgamingfreaks.model.dto.UpdateMediaEntryDTO;
import at.pcgamingfreaks.service.media.MediaLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("media")
@RequiredArgsConstructor
public class MediaController {

	private final MediaLibraryService  mediaLibraryService;

	/**
	 * Fetch data for username, service and type.
	 * If no data is synced yet and auto sync is active, pull data before returning result.
	 *
	 * @return mapped third-party data ordered by score descending
	 */
	@GetMapping("{username}/{service}/{type}")
	public ResponseEntity<List<MediaEntryDTO>> fetch(@AuthenticationPrincipal UserPrincipal requester,
	                                                 @PathVariable String username,
	                                                 @PathVariable MediaSource service,
	                                                 @PathVariable MediaType type) {
		return ResponseEntity.ok(mediaLibraryService.fetchLocal(username, service, type, requester == null ? null : requester.getUsername()));
	}

	/**
	 * Update score and state for resource.
	 * If auto sync is active, send changes directly to third-party service.
	 *
	 * @param request
	 */
	@PostMapping("{username}/{source}/{type}/update")
	@PreAuthorize("authentication.principal.username == #username")
	public void update(@PathVariable String username,
					   @PathVariable MediaSource source,
					   @PathVariable MediaType type,
					   @RequestBody UpdateMediaEntryDTO request) {
		mediaLibraryService.updateLocal(username, source, type, request);
	}

}
