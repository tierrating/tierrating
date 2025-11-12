package at.pcgamingfreaks.service.thirdpartyconnector;

import at.pcgamingfreaks.model.ThirdPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ThirdPartyConnectorFactory {
    private final Map<ThirdPartyService, ThirdPartyConnectorService> providers;

    @Autowired
    public ThirdPartyConnectorFactory(List<ThirdPartyConnectorService> providerList) {
        providers = providerList.stream()
                .collect(Collectors.toMap(ThirdPartyConnectorService::getService, provider -> provider));
    }

    public ThirdPartyConnectorService getProvider(ThirdPartyService service) {
        ThirdPartyConnectorService provider = providers.get(service);
        if (provider == null) {
            throw new IllegalArgumentException("Third party service not found: " + service);
        }
        return provider;
    }
}
