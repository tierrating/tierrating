package at.pcgamingfreaks.service;

import at.pcgamingfreaks.mapper.TierDtoMapper;
import at.pcgamingfreaks.model.db.Tier;
import at.pcgamingfreaks.model.db.Tierlist;
import at.pcgamingfreaks.model.db.User;
import at.pcgamingfreaks.model.dto.TierDTO;
import at.pcgamingfreaks.model.enums.MediaSource;
import at.pcgamingfreaks.model.enums.MediaType;
import at.pcgamingfreaks.exceptions.MediaSourceUnconfiguredException;
import at.pcgamingfreaks.exceptions.MediaNotVisibleException;
import at.pcgamingfreaks.model.repo.TierlistRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TierlistService {

	private final UserRepository userRepository;
	private final TierlistRepository tierlistRepository;
	private final DefaultTierlistProvider defaultTierlistProvider;
	private final MediaVisibilityService mediaVisibilityService;

	public List<TierDTO> getTierlist(String username, MediaSource source, MediaType type, String requesterUsername) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
		if (!user.hasMediaSourceConnection(source)) throw new MediaSourceUnconfiguredException(source);
		if (!mediaVisibilityService.isViewableBy(user, requesterUsername, source, type)) throw new MediaNotVisibleException(username);

		Optional<Tierlist> tierlist = tierlistRepository.findByUserAndServiceAndType(user, source, type);
		List<Tier> tiers = tierlist.isPresent() ? tierlist.get().getTiers() : defaultTierlistProvider.getDefaultTierlist(source, type);

		return tiers.stream()
				.map(TierDtoMapper::map)
				.sorted(Comparator.comparing(TierDTO::getScore).reversed())
				.collect(Collectors.toList());
	}

	@Transactional
	public void updateTierlist(String username, MediaSource source, MediaType type, List<TierDTO> changedTierlist) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
		if (!user.hasMediaSourceConnection(source)) throw new MediaSourceUnconfiguredException(source);

		List<Tier> mappedTiers = changedTierlist.stream().map(TierDtoMapper::map).toList();

		Tierlist tierlist = tierlistRepository.findByUserAndServiceAndType(user, source, type).orElseGet(Tierlist::new);
		if (mappedTiers.equals(defaultTierlistProvider.getDefaultTierlist(source, type))) {
			if (tierlist.getTiers().isEmpty()) return;
			tierlist.getTiers().clear();
		} else {
			tierlist.setUser(user);
			tierlist.setService(source);
			tierlist.setType(type);

			tierlist.getTiers().clear();
			tierlist.getTiers().addAll(mappedTiers);
		}

		tierlistRepository.save(tierlist);
	}
}
