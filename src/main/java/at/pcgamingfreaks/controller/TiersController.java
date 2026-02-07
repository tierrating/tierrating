package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.dto.TierDTO;
import at.pcgamingfreaks.service.TiersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("tiers")
@RequiredArgsConstructor
public class TiersController {
    private final TiersService tiersService;

    /**
     * @return list of tier grades sorted by score
     */
    @GetMapping("{username}/{service}/{type}")
    public ResponseEntity<List<TierDTO>> getTierlist(@PathVariable String username, @PathVariable ThirdPartyService service, @PathVariable ContentType type) {
        return ResponseEntity.ok(tiersService.getTierlist(username, service, type));
    }

    @PostMapping("{username}/{service}/{type}")
    @PreAuthorize("authentication.principal.username == #username")
    public void setTierlist(@PathVariable String username, @PathVariable ThirdPartyService service,
                            @PathVariable ContentType type, @RequestBody List<TierDTO> changedTierlist) {
        tiersService.updateTierlist(username, service, type, changedTierlist);
    }
}
