package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.Service;
import at.pcgamingfreaks.model.Tier;
import at.pcgamingfreaks.model.TierList;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.repo.TierListsRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("tiers")
@RequiredArgsConstructor
public class TiersController {
    private final UserRepository userRepository;
    private final TierListsRepository tierListsRepository;

    @GetMapping("{username}/{service}/{type}")
    public ResponseEntity<List<Tier>> getTierlist(@PathVariable String username, @PathVariable Service service, @PathVariable ContentType type) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty() || !hasUserConnection(user.get(), service)) {
            return ResponseEntity.notFound().build();
        }
        Optional<TierList> tierlist = tierListsRepository.findByUserAndServiceAndType(user.get(), service, type);

        return tierlist.map(tierList -> ResponseEntity.ok(tierList.getTiers().stream()
                .sorted(Comparator.comparing(Tier::getScore).reversed())
                .toList()
        )).orElseGet(() -> ResponseEntity.ok(new ArrayList<>()));

    }

    @PostMapping("{username}/{service}/{type}")
    public ResponseEntity<?> setTierlist(@PathVariable String username, @PathVariable Service service,
                             @PathVariable ContentType type, @RequestBody List<Tier> changedTierlist) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty() || !hasUserConnection(user.get(), service)) {
            return ResponseEntity.notFound().build();
        }
        Optional<TierList> existingTierlist = tierListsRepository.findByUserAndServiceAndType(user.get(), service, type);

        if (existingTierlist.isPresent()) {
            existingTierlist.get().setTiers(changedTierlist);
            tierListsRepository.save(existingTierlist.get());
        } else {
            TierList tierlist = new TierList();
            tierlist.setUser(user.get());
            tierlist.setService(service);
            tierlist.setType(type);
            tierlist.setTiers(changedTierlist);
            tierListsRepository.save(tierlist);
        }

        return ResponseEntity.ok().build();
    }

    private boolean hasUserConnection(User user, Service service) {
        return switch (service) {
            case ANILIST -> user.getAnilistConnection() != null;
            default ->  false;
        };

    }
}
