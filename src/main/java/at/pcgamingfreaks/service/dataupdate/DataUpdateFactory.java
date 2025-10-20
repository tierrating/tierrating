package at.pcgamingfreaks.service.dataupdate;

import at.pcgamingfreaks.model.ThirdPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataUpdateFactory {
    private final Map<ThirdPartyService, DataUpdateService> providers;

    @Autowired
    public DataUpdateFactory(List<DataUpdateService> providerList) {
        providers = providerList.stream()
                .collect(Collectors.toMap(DataUpdateService::getService, dataUpdateService -> dataUpdateService));
    }

    public DataUpdateService getProvider(ThirdPartyService service) {
        DataUpdateService provider = providers.get(service);
        if (provider == null) {
            throw new IllegalArgumentException("No provider found for service: " + service);
        }
        return provider;
    }
}
