package at.pcgamingfreaks.service.thirdpartyconnector;

import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ThirdPartyRemovalResponseDTO;
import at.pcgamingfreaks.model.repo.ThirdpartyConnectionRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnilistConnectorService implements ThirdPartyConnectorService {
    private final ThirdpartyConnectionRepository  thirdpartyConnectionRepository;
    private final UserRepository userRepository;

    @Override
    public ThirdPartyService getService() {
        return ThirdPartyService.ANILIST;
    }

    @Override
    public ThirdPartyRemovalResponseDTO removeConnection(User user) {
        try {
            thirdpartyConnectionRepository.deleteById(user.getAnilistConnection().getId());
            user.setAnilistConnection(null);
            userRepository.save(user);
            return new ThirdPartyRemovalResponseDTO(true, "");
        } catch (Exception e) {
            return new ThirdPartyRemovalResponseDTO(false, e.getMessage());
        }
    }
}
