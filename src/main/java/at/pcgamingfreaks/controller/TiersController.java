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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("tiers")
@RequiredArgsConstructor
public class TiersController {
    private final UserRepository userRepository;
    private final TierListsRepository tierListsRepository;

    @GetMapping("{username}/{service}/{type}")
    private ResponseEntity<List<Tier>> getTierlist(@PathVariable String username, @PathVariable Service service, @PathVariable ContentType type) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty() || !hasUserConnection(user.get(), service)) {
            return ResponseEntity.notFound().build();
        }
        List<Tier> tiers = tierListsRepository.findByUserAndServiceAndType(user.get(), service, type)
                .map(TierList::getTiers)
                .orElse(getDefaultTiers())
                .stream()
                .sorted(Comparator.comparing(Tier::getScore).reversed())
                .toList();
        return ResponseEntity.ok(tiers);
    }

    private List<Tier> getDefaultTiers() {
        return List.of(
                new Tier("#FF7F7F", "S", 10.0, 10.0),       // Red
                new Tier("#FF9E7F", "A+", 9.0, 9.0),        // Light Orange (between Red and Orange)
                new Tier("#FFBF7F", "A", 8.0, 8.0),         // Orange
                new Tier("#FFDF7F", "A-", 7.5, 7.5),        // Light Yellow-Orange (between Orange and Yellow)
                new Tier("#FFFF7F", "B+", 7.0, 7.0),        // Yellow
                new Tier("#BFFF7F", "B", 6.0, 6.0),         // Light Green-Yellow (between Yellow and Green)
                new Tier("#7FFF7F", "B-", 5.5, 5.5),        // Green
                new Tier("#7FDFBF", "C+", 5.0, 5.0),        // Teal (between Green and Blue)
                new Tier("#7FBFFF", "C", 4.0, 4.0),         // Blue
                new Tier("#9F7FFF", "D", 3.0, 3.0),         // Light Purple (between Blue and Purple)
                new Tier("#BF7FFF", "E", 2.0, 2.0),         // Purple
                new Tier("#FF7FBF", "F", 1.0, 1.0),         // Pink
                new Tier("#E6E6FF", "Unassigned", 0.0, 0.0) // Light Grey
        );
    }

    private boolean hasUserConnection(User user, Service service) {
        return switch (service) {
            case ANILIST -> user.getAnilistConnection() != null;
            default ->  false;
        };

    }
}
