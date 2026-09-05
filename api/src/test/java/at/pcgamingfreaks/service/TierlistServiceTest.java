package at.pcgamingfreaks.service;

import at.pcgamingfreaks.model.db.MediaSourceConnection;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TierlistServiceTest {

	@Mock
	UserRepository userRepository;

	@Mock
	TierlistRepository tierlistRepository;

	@Mock
	DefaultTierlistProvider defaultTierlistProvider;

	@Mock
	MediaVisibilityService mediaVisibilityService;

	@InjectMocks
	TierlistService underTest;

	@Test
	void getTierlist_userNotFound() {
		when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
		assertThrows(UsernameNotFoundException.class, () -> underTest.getTierlist("test", null, null, "test"));
	}

	@Test
	void getTierlist_userHasNoConnection() {
		User user = new User();
		when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
		assertThrows(MediaSourceUnconfiguredException.class, () -> underTest.getTierlist("test", MediaSource.ANILIST, MediaType.ANIME, "test"));
	}

	@Test
	void getTierlist_notVisibleForOtherUsers() {
		User user = new User();
		user.getConnections().put(MediaSource.ANILIST, new MediaSourceConnection());

		when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
		when(mediaVisibilityService.isViewableBy(user, "someone-else", MediaSource.ANILIST, MediaType.ANIME)).thenReturn(false);

		assertThrows(MediaNotVisibleException.class, () -> underTest.getTierlist("test", MediaSource.ANILIST, MediaType.ANIME, "someone-else"));
	}

	@Test
	void getTierlist_getDefaults() {
		User user = new User();
		user.getConnections().put(MediaSource.ANILIST, new MediaSourceConnection());
		List<Tier> defaultTiers = List.of(new Tier("S", "#000000", 10.0, 10.0));

		when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
		when(mediaVisibilityService.isViewableBy(user, "test", MediaSource.ANILIST, MediaType.ANIME)).thenReturn(true);
		when(tierlistRepository.findByUserAndServiceAndType(user, MediaSource.ANILIST, MediaType.ANIME)).thenReturn(Optional.empty());
		when(defaultTierlistProvider.getDefaultTierlist(MediaSource.ANILIST, MediaType.ANIME)).thenReturn(defaultTiers);

		List<TierDTO> result = underTest.getTierlist("test", MediaSource.ANILIST, MediaType.ANIME, "test");

		assertEquals(List.of(new TierDTO("S", "#000000", 10.0, 10.0)), result);
	}

	@Test
	void getTierlist_userSpecific() {
		User user = new User();
		user.getConnections().put(MediaSource.ANILIST, new MediaSourceConnection());
		List<Tier> userSpecificTiers = List.of(new Tier("S", "#000000", 10.0, 10.0));
		Tierlist tierlist = new Tierlist();
		tierlist.setTiers(userSpecificTiers);

		when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
		when(mediaVisibilityService.isViewableBy(user, "test", MediaSource.ANILIST, MediaType.ANIME)).thenReturn(true);
		when(tierlistRepository.findByUserAndServiceAndType(user, MediaSource.ANILIST, MediaType.ANIME)).thenReturn(Optional.of(tierlist));

		List<TierDTO> result = underTest.getTierlist("test", MediaSource.ANILIST, MediaType.ANIME, "test");

		assertEquals(List.of(new TierDTO("S", "#000000", 10.0, 10.0)), result);
	}

	@Test
	void updateTierlist_userHasNoConnection() {
		User user = new User();
		when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
		assertThrows(MediaSourceUnconfiguredException.class, () -> underTest.updateTierlist("test", MediaSource.ANILIST, null, List.of()));
	}

	@Test
	void updateTierlist_skipPersisting() {
		User user = new User();
		user.getConnections().put(MediaSource.ANILIST, new MediaSourceConnection());
		List<Tier> defaultTiers = List.of(new Tier("S", "#000000", 10.0, 10.0));

		when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
		when(tierlistRepository.findByUserAndServiceAndType(user, MediaSource.ANILIST, MediaType.ANIME)).thenReturn(Optional.empty());
		when(defaultTierlistProvider.getDefaultTierlist(MediaSource.ANILIST, MediaType.ANIME)).thenReturn(defaultTiers);

		underTest.updateTierlist("test", MediaSource.ANILIST, MediaType.ANIME, List.of(new TierDTO("S", "#000000", 10.0, 10.0)));

		verify(tierlistRepository, times(0)).save(any());
	}

	@Test
	void updateTierlist_clearExisting() {
		User user = new User();
		user.getConnections().put(MediaSource.ANILIST, new MediaSourceConnection());
		List<Tier> defaultTiers = List.of(new Tier("S", "#000000", 10.0, 10.0));
		Tierlist tierlist = new Tierlist();
		tierlist.setTiers(new ArrayList<>(List.of(new Tier("A", "#FFFFFF", 9, 9))));

		when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
		when(tierlistRepository.findByUserAndServiceAndType(user, MediaSource.ANILIST, MediaType.ANIME)).thenReturn(Optional.of(tierlist));
		when(defaultTierlistProvider.getDefaultTierlist(MediaSource.ANILIST, MediaType.ANIME)).thenReturn(defaultTiers);

		underTest.updateTierlist("test", MediaSource.ANILIST, MediaType.ANIME, List.of(new TierDTO("S", "#000000", 10.0, 10.0)));

		ArgumentCaptor<Tierlist> captor = ArgumentCaptor.forClass(Tierlist.class);
		verify(tierlistRepository, times(1)).save(captor.capture());
		assertThat(captor.getValue().getTiers()).isEmpty();
	}

	@Test
	void updateTierlist_overwriteExisting() {
		User user = new User();
		user.getConnections().put(MediaSource.ANILIST, new MediaSourceConnection());
		List<Tier> defaultTiers = List.of(new Tier("S", "#000000", 10.0, 10.0));
		Tierlist tierlist = new Tierlist();
		tierlist.setTiers(new ArrayList<>(List.of(new Tier("A", "#FFFFFF", 9, 9))));

		when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
		when(tierlistRepository.findByUserAndServiceAndType(user, MediaSource.ANILIST, MediaType.ANIME)).thenReturn(Optional.of(tierlist));
		when(defaultTierlistProvider.getDefaultTierlist(MediaSource.ANILIST, MediaType.ANIME)).thenReturn(defaultTiers);

		underTest.updateTierlist("test", MediaSource.ANILIST, MediaType.ANIME, List.of(new TierDTO("B", "#CCCCCC", 7, 7)));

		ArgumentCaptor<Tierlist> captor = ArgumentCaptor.forClass(Tierlist.class);
		verify(tierlistRepository, times(1)).save(captor.capture());
		assertThat(captor.getValue().getTiers()).isEqualTo(List.of(new Tier("B", "#CCCCCC", 7, 7)));
	}
}