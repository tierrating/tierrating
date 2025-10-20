package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.mapper.TierDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.Tier;
import at.pcgamingfreaks.model.TierList;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.TierDTO;
import at.pcgamingfreaks.model.repo.TierListsRepository;
import at.pcgamingfreaks.model.repo.TiersRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static at.pcgamingfreaks.model.ThirdPartyService.hasUserConnection;

@Slf4j
@RestController
@RequestMapping("tiers")
@RequiredArgsConstructor
public class TiersController {
    private final UserRepository userRepository;
    private final TierListsRepository tierListsRepository;
    private final TiersRepository tiersRepository;

    @GetMapping("{username}/{service}/{type}")
    public ResponseEntity<List<TierDTO>> getTierlist(@PathVariable String username, @PathVariable ThirdPartyService service, @PathVariable ContentType type) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty() || !hasUserConnection(user.get(), service)) {
            return ResponseEntity.notFound().build();
        }
        Optional<TierList> tierlist = tierListsRepository.findByUserAndServiceAndType(user.get(), service, type);

        List<TierDTO> tiers = tierlist.map(tierList -> tierList.getTiers().stream()
                .map(TierDtoMapper::map)
                .sorted(Comparator.comparing(TierDTO::getScore).reversed())
                .collect(Collectors.toList())
        ).orElseGet(ArrayList::new);

        return ResponseEntity.ok(tiers);
    }

    @PostMapping("{username}/{service}/{type}")
    public ResponseEntity<?> setTierlist(@PathVariable String username, @PathVariable ThirdPartyService service,
                             @PathVariable ContentType type, @RequestBody List<TierDTO> changedTierlist) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty() || !hasUserConnection(user.get(), service)) {
            return ResponseEntity.notFound().build();
        }
        Optional<TierList> existingTierlist = tierListsRepository.findByUserAndServiceAndType(user.get(), service, type);
        try {
            if (existingTierlist.isPresent()) {
                Set<UUID> changedTiers = changedTierlist.stream().map(TierDTO::getId).collect(Collectors.toSet());
                List<Tier> existingTiers = existingTierlist.get().getTiers();
                List<Tier> removedTiers = new ArrayList<>();
                // handle removed tiers
                int i = 0;
                while (i < existingTiers.size()) {
                    if (!changedTiers.contains(existingTiers.get(i).getId())) {
                        removedTiers.add(existingTiers.get(i));
                        existingTiers.remove(i);
                    } else {
                        i++;
                    }
                }

                // handle added and modified tiers
                Map<UUID, Tier> existingTiersMap = existingTierlist.get().getTiers().stream().collect(Collectors.toMap(Tier::getId, Function.identity()));
                for (TierDTO changedTier: changedTierlist) {
                    if (existingTiersMap.containsKey(changedTier.getId())) {
                        Tier tier = existingTiersMap.get(changedTier.getId());
                        tier.setName(changedTier.getName());
                        tier.setColor(changedTier.getColor());
                        tier.setScore(changedTier.getScore());
                        tier.setAdjustedScore(changedTier.getAdjustedScore());
                    } else {
                        Tier tier = TierDtoMapper.map(changedTier);
                        tier.setTierlist(existingTierlist.get());
                        existingTiers.add(tier);
                    }
                }

                tierListsRepository.save(existingTierlist.get());
                tiersRepository.deleteAll(removedTiers);
            } else {
                TierList tierlist = new TierList();
                tierlist.setUser(user.get());
                tierlist.setService(service);
                tierlist.setType(type);

                tierlist.setTiers(new ArrayList<>());
                // recreate tiers to be mapped by JPA
                changedTierlist.forEach(tier -> {
                            Tier newTier = TierDtoMapper.map(tier);
                            newTier.setTierlist(tierlist);
                            tierlist.getTiers().add(newTier);
                        });

                tierListsRepository.save(tierlist);
            }
        } catch (Exception e) {
            log.error("", e);
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }


}
