package at.pcgamingfreaks.model.repo;

import at.pcgamingfreaks.model.TmdbCoverCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TmdbCoverCacheRepository extends JpaRepository<TmdbCoverCache, Long> {
    Optional<TmdbCoverCache> findByIdAndSeason(long id, Long season);
}
