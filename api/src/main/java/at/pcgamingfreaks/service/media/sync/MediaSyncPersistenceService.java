package at.pcgamingfreaks.service.media.sync;

import at.pcgamingfreaks.model.RemoteSyncResult;
import at.pcgamingfreaks.model.db.User;
import at.pcgamingfreaks.model.db.media.MediaEntry;
import at.pcgamingfreaks.model.db.media.UserMediaEntryState;
import at.pcgamingfreaks.model.enums.MediaSource;
import at.pcgamingfreaks.model.repo.*;
import at.pcgamingfreaks.service.media.remote.RemoteMediaClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaSyncPersistenceService {
	private final UserRepository userRepository;

	private final UserMediaEntryStateRepository userMediaEntryStateRepository;

	private final AnilistMediaEntryRepository anilistMediaEntryRepository;
	private final TraktMediaEntryRepository traktMediaEntryRepository;
	private final SteamMediaEntryRepository steamMediaEntryRepository;

	@Transactional
	public <E extends MediaEntry> void reconcile(Long userId, MediaSource source,
	                                                RemoteMediaClient<E> remoteMediaClient,
	                                                List<RemoteSyncResult<E>> remoteEntries) {
		User userProxy = userRepository.getReferenceById(userId);
		MediaEntryRepository<E> mediaEntryRepository = resolveRepository(source);

		Set<Long> remoteIds = remoteEntries.stream()
				.map(result -> result.entry().getId())
				.collect(Collectors.toSet());

		Map<Long, E> localEntries = mediaEntryRepository.findAllByIdIn(remoteIds).stream()
				.collect(Collectors.toMap(MediaEntry::getId, Function.identity()));
		Map<Long, UserMediaEntryState> localStates = userMediaEntryStateRepository
				.findAllByUserAndSource(userProxy, source)
				.stream()
				.collect(Collectors.toMap(UserMediaEntryState::getEntryId, Function.identity()));

		List<E> entriesToSave = new ArrayList<>();
		List<UserMediaEntryState> statesToSave = new ArrayList<>();
		for (RemoteSyncResult<E> remoteResult : remoteEntries) {
			E remoteEntry = remoteResult.entry();
			E localEntry = localEntries.get(remoteEntry.getId());
			UserMediaEntryState localState = localStates.get(remoteEntry.getId());

			E entryToSave = updateOrCreate(remoteEntry, localEntry);
			if (entryToSave != null) entriesToSave.add(entryToSave);

			UserMediaEntryState stateToSave = updateOrCreate(remoteResult, localState, source, userProxy, remoteMediaClient);
			if (stateToSave != null) statesToSave.add(stateToSave);
		}
		mediaEntryRepository.saveAll(entriesToSave);
		userMediaEntryStateRepository.saveAll(statesToSave);
	}

	private <E extends MediaEntry> E updateOrCreate(E remoteEntry, E localEntry) {
		if (localEntry == null) {
			return remoteEntry;
		}

		boolean needsUpdate = !Objects.equals(localEntry.getTitle(), remoteEntry.getTitle())
				|| !Objects.equals(localEntry.getCoverUrl(), remoteEntry.getCoverUrl());

		if (needsUpdate) {
			localEntry.setTitle(remoteEntry.getTitle());
			localEntry.setCoverUrl(remoteEntry.getCoverUrl());
			return localEntry;
		}
		return null;
	}

	private <E extends MediaEntry> UserMediaEntryState updateOrCreate(RemoteSyncResult<E> remoteResult, UserMediaEntryState localState, MediaSource source, User userProxy, RemoteMediaClient<E> remoteMediaClient) {
		if (localState == null) {
			UserMediaEntryState newState = new UserMediaEntryState();
			newState.setEntryId(remoteResult.entry().getId());
			newState.setSource(source);
			newState.setUser(userProxy);
			newState.setScore(remoteResult.score());
			newState.setState(remoteResult.status());
			return newState;
		}

		if (remoteMediaClient.shouldOverwriteLocal(localState.getScore(), remoteResult.score())
				&& Objects.equals(localState.getState(), remoteResult.status())) {
			return null;
		}

		localState.setScore(remoteResult.score());
		localState.setState(remoteResult.status());
		return localState;
	}

	@SuppressWarnings("unchecked")
	private <E extends MediaEntry> MediaEntryRepository<E> resolveRepository(MediaSource source) {
		return switch (source) {
			case ANILIST -> (MediaEntryRepository<E>) anilistMediaEntryRepository;
			case TRAKT -> (MediaEntryRepository<E>) traktMediaEntryRepository;
			case STEAM -> (MediaEntryRepository<E>) steamMediaEntryRepository;
			default -> throw new IllegalArgumentException("Unsupported source: " + source);
		};
	}
}
