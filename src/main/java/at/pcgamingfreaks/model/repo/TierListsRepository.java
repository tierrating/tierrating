package at.pcgamingfreaks.model.repo;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.Service;
import at.pcgamingfreaks.model.TierList;
import at.pcgamingfreaks.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TierListsRepository extends JpaRepository<TierList, Long> {

    Optional<TierList> findByUserAndServiceAndType(User user, Service service, ContentType type);
}
