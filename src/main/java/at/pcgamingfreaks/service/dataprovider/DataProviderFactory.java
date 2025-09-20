package at.pcgamingfreaks.service.dataprovider;

import at.pcgamingfreaks.model.ThirdPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataProviderFactory {
    private final Map<ThirdPartyService, DataProviderService> providers;

    @Autowired
    public DataProviderFactory(List<DataProviderService> providerList) {
        providers = providerList.stream()
                .collect(Collectors.toMap(DataProviderService::getService, dataProviderService -> dataProviderService));
    }

    public DataProviderService getProvider(ThirdPartyService service) {
        DataProviderService provider = providers.get(service);
        if (provider == null) {
            throw new IllegalArgumentException("No provider found for service: " + service);
        }
        return provider;
    }
}
