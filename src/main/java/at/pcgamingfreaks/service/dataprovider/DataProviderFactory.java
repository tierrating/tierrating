package at.pcgamingfreaks.service.dataprovider;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataProviderFactory {
    private final Map<ThirdPartyService, Map<ContentType, DataProviderService>> providers;

    @Autowired
    public DataProviderFactory(List<DataProviderService> providerList) {
        Map<ThirdPartyService, List<DataProviderService>> providersByService = providerList.stream()
                .collect(Collectors.groupingBy(DataProviderService::getService));
        providers = providersByService.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .collect(Collectors.toMap(DataProviderService::getContentType, provider -> provider))));
    }

    public DataProviderService getProvider(ThirdPartyService service, ContentType contentType) {
        DataProviderService provider = providers.containsKey(service) ? providers.get(service).get(contentType) : null;
        if (provider == null) {
            throw new IllegalArgumentException("No provider found for service: " + service);
        }
        return provider;
    }
}
