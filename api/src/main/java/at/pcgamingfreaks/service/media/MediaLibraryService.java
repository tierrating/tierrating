package at.pcgamingfreaks.service.media;

import at.pcgamingfreaks.exceptions.MediaNotVisibleException;
import at.pcgamingfreaks.exceptions.UnknownMediaEntryException;
import at.pcgamingfreaks.mapper.mediaentry.MediaEntryMapper;
import at.pcgamingfreaks.mapper.mediaentry.MediaEntryMapperRegistry;
import at.pcgamingfreaks.model.RemoteUpdateEntry;
import at.pcgamingfreaks.model.db.User;
import at.pcgamingfreaks.model.db.media.MediaEntry;
import at.pcgamingfreaks.model.db.media.UserMediaEntryState;
import at.pcgamingfreaks.model.dto.MediaEntryDTO;
import at.pcgamingfreaks.model.dto.UpdateMediaEntryDTO;
import at.pcgamingfreaks.model.enums.MediaSource;
import at.pcgamingfreaks.model.enums.MediaState;
import at.pcgamingfreaks.model.enums.MediaType;
import at.pcgamingfreaks.model.repo.MediaEntryRepository;
import at.pcgamingfreaks.model.repo.UserMediaEntryStateRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import at.pcgamingfreaks.service.MediaVisibilityService;
import at.pcgamingfreaks.service.media.remote.RemoteClientRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaLibraryService {

	private final UserRepository userRepository;
	private final UserMediaEntryStateRepository userMediaEntryStateRepository;
	private final MediaEntryRepositoryRegistry mediaEntryRepositoryRegistry;
	private final MediaEntryMapperRegistry mediaEntryMapperRegistry;
	private final RemoteClientRegistry remoteClientRegistry;
	private final MediaVisibilityService mediaVisibilityService;

	public <E extends MediaEntry> List<MediaEntryDTO> fetchLocal(String username, MediaSource source, MediaType type, String requesterUsername) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
		if (!mediaVisibilityService.isViewableBy(user, requesterUsername, source, type)) throw new MediaNotVisibleException(username);

		// TODO: restrict lookup to specific states
		Map<Long, UserMediaEntryState> userStatesByMediaEntryId = userMediaEntryStateRepository.findAllByUserAndSource(user, source)
				.stream()
				.collect(Collectors.toMap(UserMediaEntryState::getEntryId, Function.identity()));

		MediaEntryRepository<E> mediaEntryRepository = mediaEntryRepositoryRegistry.getRepository(source);
		List<E> mediaEntries = mediaEntryRepository.findAllByIdInAndType(userStatesByMediaEntryId.keySet(), type);

		Set<MediaState> hiddenStates = mediaVisibilityService.hiddenStatesFor(user, requesterUsername, source, type);
		MediaEntryMapper<E> mapper = mediaEntryMapperRegistry.getMapper(source);
		return mediaEntries.stream()
				.filter(entry -> {
					UserMediaEntryState state = userStatesByMediaEntryId.get(entry.getId());
					return state == null || state.getState() == null || !hiddenStates.contains(state.getState());
				})
				.map(entry -> mapper.toDTO(entry, userStatesByMediaEntryId.get(entry.getId())))
				.sorted(Comparator.comparing(MediaEntryDTO::getScore).reversed())
				.toList();
	}

	@Transactional
	public void updateLocal(String username, MediaSource source, MediaType type, UpdateMediaEntryDTO request) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
		Optional<UserMediaEntryState> existingUserState = userMediaEntryStateRepository.findByUserAndSourceAndEntryId(user, source, request.getId());
		UserMediaEntryState userState = existingUserState.orElse(new UserMediaEntryState());
		if (existingUserState.isEmpty()) {
			userState.setUser(user);
			userState.setSource(source);
			mediaEntryRepositoryRegistry.getRepository(source).findById(request.getId())
					.orElseThrow(() -> new UnknownMediaEntryException(request.getId()));
			userState.setEntryId(request.getId());
		}
		userState.setScore(request.getScore());
		userState.setState(request.getState());
		userMediaEntryStateRepository.save(userState);

		// TODO: should this be done directly here? dirtyState would be a idea, would work nicely with push function. sync could be async
//		if (user.getConnections().get(source).getMediaTypeSettings().get(type).isAutoPush()) {
//			remoteClientRegistry.getClient(source, type).pushRemote(
//					user.getConnections().get(source),
//					List.of(new RemoteUpdateEntry(request.getId(), request.getScore(), request.getState()))
//			);
//		}
	}
}
