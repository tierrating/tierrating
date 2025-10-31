package at.pcgamingfreaks.service.dataprovider;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.dto.ListEntryDTO;

import java.util.List;

public interface DataProviderService {
    ThirdPartyService getService();

    default boolean isTypeAllowed(ContentType type) {
        return getService().getAllowedTypes().contains(type);
    }

    List<ListEntryDTO> fetchData(String username, ContentType type);
}
