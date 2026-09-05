package at.pcgamingfreaks.service.media.sync;

import at.pcgamingfreaks.model.db.SyncJob;
import at.pcgamingfreaks.model.enums.SyncStatus;
import at.pcgamingfreaks.model.repo.SyncJobRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


@Slf4j
@AllArgsConstructor
public class MediaSyncJob implements Runnable {
	private SyncJob syncJob;
	private final MediaSyncProcessor processor;
	private final SyncJobRepository syncJobRepository;

	@Override
	public void run() {
		long startingTime = System.currentTimeMillis();
		try {
			log.debug("Starting sync for {} {} {}",
					syncJob.getUser().getUsername(), syncJob.getMediaSource(), syncJob.getMediaType());

			syncJobRepository.updateStatus(syncJob.getId(), SyncStatus.IN_PROGRESS);

			processor.processSync(syncJob.getUser().getId(), syncJob.getMediaSource(), syncJob.getMediaType());

			syncJobRepository.completeJob(syncJob.getId(), SyncStatus.COMPLETED, LocalDateTime.now());
			log.debug("Completed sync for {} {} {} in {}ms",
					syncJob.getUser().getUsername(), syncJob.getMediaSource(), syncJob.getMediaType(),
					System.currentTimeMillis() - startingTime);
		} catch (Exception e) {
			syncJobRepository.completeJob(syncJob.getId(), SyncStatus.FAILED, LocalDateTime.now());
			log.error("Sync failed for {} {} {} after {}ms",
					syncJob.getUser().getUsername(), syncJob.getMediaSource(), syncJob.getMediaType(),
					System.currentTimeMillis() - startingTime, e);
		}
	}
}
