package at.pcgamingfreaks.service.dataupdate;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DataUpdateFactory {
    private final Map<ThirdPartyService, Map<ContentType, DataUpdateService>> providers;

    @Autowired
    public DataUpdateFactory(List<DataUpdateService> providerList) {
        Map<ThirdPartyService, List<DataUpdateService>> providersByService = providerList.stream()
                .collect(Collectors.groupingBy(DataUpdateService::getService));
        providers = providersByService.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .collect(Collectors.toMap(DataUpdateService::getContentType, provider -> provider))));
    }

    public DataUpdateService getProvider(ThirdPartyService service, ContentType contentType) {
        DataUpdateService provider = providers.containsKey(service) ? providers.get(service).get(contentType) : null;
        if (provider == null) {
            throw new IllegalArgumentException(String.format("No provider found for service %s and type %s", service, contentType));
        }
        return provider;
    }
}
