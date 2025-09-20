package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.service.dataprovider.DataProviderFactory;
import at.pcgamingfreaks.service.dataprovider.DataProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("data")
@CrossOrigin
@RequiredArgsConstructor
public class DataController {
    private final DataProviderFactory dataProviderFactory;

    @GetMapping("{username}/{service}/{type}")
    public ResponseEntity<List<ListEntryDTO>> fetchData(@PathVariable String username,
                                                        @PathVariable ThirdPartyService service,
                                                        @PathVariable ContentType type) {
        DataProviderService dataProviderService = dataProviderFactory.getProvider(service);
        return ResponseEntity.ok(dataProviderService.fetchData(username, type));
    }
}
