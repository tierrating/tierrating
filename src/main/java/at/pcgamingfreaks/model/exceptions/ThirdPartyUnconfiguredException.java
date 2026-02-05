package at.pcgamingfreaks.model.exceptions;

import at.pcgamingfreaks.model.ThirdPartyService;
import lombok.Getter;

public class ThirdPartyUnconfiguredException extends RuntimeException {

    @Getter
    private final ThirdPartyService unconfiguredService;

    public ThirdPartyUnconfiguredException(ThirdPartyService unconfiguredService) {
        this.unconfiguredService = unconfiguredService;
    }
}
