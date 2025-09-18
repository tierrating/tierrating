package at.pcgamingfreaks.model.repo;

import at.pcgamingfreaks.model.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TiersRepository extends JpaRepository<Tier, UUID> {
}
