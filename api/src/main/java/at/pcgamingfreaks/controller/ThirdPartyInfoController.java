package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.config.ThirdPartyConfig;
import at.pcgamingfreaks.config.ThirdPartyServiceConfig;
import at.pcgamingfreaks.model.enums.MediaSource;
import at.pcgamingfreaks.model.dto.ThirdPartyInfoResponseDTO;
import at.pcgamingfreaks.service.info.ThirdPartyInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("info")
@RequiredArgsConstructor
public class ThirdPartyInfoController {
	private final ThirdPartyInfoFactory thirdPartyInfoFactory;
	private final ThirdPartyConfig thirdPartyConfig;

	@GetMapping("{service}")
	public ResponseEntity<ThirdPartyInfoResponseDTO> info(@PathVariable MediaSource service) {
		return ResponseEntity.ok(thirdPartyInfoFactory.getProvider(service).info());
	}

	@GetMapping("services")
	public ResponseEntity<List<String>> getAvailableServices() {
		List<MediaSource> services = new ArrayList<>();
		if (isConfigured(thirdPartyConfig.getAnilist())) services.add(MediaSource.ANILIST);
		if (isConfigured(thirdPartyConfig.getTrakt())) services.add(MediaSource.TRAKT);
		if (isConfigured(thirdPartyConfig.getSteam())) services.add(MediaSource.STEAM);
		return ResponseEntity.ok(services.stream().map(MediaSource::name).toList());
	}

	private boolean isConfigured(ThirdPartyServiceConfig config) {
		return config != null && config.isValid();
	}
}
