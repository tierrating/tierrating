package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.dto.UpdateScoreRequestDTO;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.model.dto.UpdateScoreResponseDTO;
import at.pcgamingfreaks.model.repo.UserRepository;
import at.pcgamingfreaks.service.dataprovider.DataProviderFactory;
import at.pcgamingfreaks.service.dataprovider.DataProviderService;
import at.pcgamingfreaks.service.dataupdate.DataUpdateFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static at.pcgamingfreaks.model.ThirdPartyService.hasUserConnection;

@Slf4j
@RestController
@RequestMapping("data")
@CrossOrigin
@RequiredArgsConstructor
public class DataController {
    private final DataProviderFactory dataProviderFactory;
    private final DataUpdateFactory dataUpdateFactory;

    private final UserRepository userRepository;

    /**
     * Request and map third-party data.
     * @return mapped third-party data ordered by score descending
     */
    @GetMapping("{username}/{service}/{type}")
    public ResponseEntity<List<ListEntryDTO>> fetchData(@PathVariable String username,
                                                        @PathVariable ThirdPartyService service,
                                                        @PathVariable ContentType type) {
        DataProviderService dataProviderService = dataProviderFactory.getProvider(service);
        if (!dataProviderService.isTypeAllowed(type)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dataProviderService.fetchData(username, type));
    }

    /**
     * Update score/rating at third-party service
     * @param request
     * @return
     */
    @PostMapping("update")
    @PreAuthorize("authentication.principal.username == #request.username")
    public ResponseEntity<UpdateScoreResponseDTO> updateData(@RequestBody UpdateScoreRequestDTO request) {
        Optional<User> user = userRepository.findByUsername(request.getUsername());
        if (user.isEmpty() || !hasUserConnection(user.get(), request.getService())) {
            return ResponseEntity.notFound().build();
        }

        try {
            dataUpdateFactory.getProvider(request.getService()).updateData(request.getId(), request.getScore(), user.get(), request.getType());
            return ResponseEntity.ok(UpdateScoreResponseDTO.success());
        }  catch (Exception e) {
            log.error("Failed updating score for {}", user.get().getUsername(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
