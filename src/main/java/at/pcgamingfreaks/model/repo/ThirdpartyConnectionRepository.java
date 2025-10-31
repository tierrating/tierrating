package at.pcgamingfreaks.model.repo;

import at.pcgamingfreaks.model.auth.ThirdPartyConnection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThirdpartyConnectionRepository extends JpaRepository<ThirdPartyConnection, Long> {

}
