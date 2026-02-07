package at.pcgamingfreaks.service;

import at.pcgamingfreaks.mapper.TierDtoMapper;
import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.ThirdPartyService;
import at.pcgamingfreaks.model.Tier;
import at.pcgamingfreaks.model.TierList;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.TierDTO;
import at.pcgamingfreaks.model.exceptions.ThirdPartyUnconfiguredException;
import at.pcgamingfreaks.model.repo.TierListsRepository;
import at.pcgamingfreaks.model.repo.TiersRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static at.pcgamingfreaks.model.ThirdPartyService.hasUserConnection;

@Service
@RequiredArgsConstructor
public class TiersService {
    private final UserRepository userRepository;
    private final TierListsRepository tierListsRepository;
    private final TiersRepository tiersRepository;

    public List<TierDTO> getTierlist(String username, ThirdPartyService service, ContentType type) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        if (!hasUserConnection(user, service)) throw new ThirdPartyUnconfiguredException(service);

        Optional<TierList> tierlist = tierListsRepository.findByUserAndServiceAndType(user, service, type);

        List<TierDTO> tiers = tierlist.map(tierList -> tierList.getTiers().stream()
                .map(TierDtoMapper::map)
                .sorted(Comparator.comparing(TierDTO::getScore).reversed())
                .collect(Collectors.toList())
        ).orElseGet(ArrayList::new);

        return tiers;
    }

    public void updateTierlist(String username, ThirdPartyService service, ContentType type, List<TierDTO> changedTierlist) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        if (!hasUserConnection(user, service)) throw new ThirdPartyUnconfiguredException(service);

        Optional<TierList> existingTierlist = tierListsRepository.findByUserAndServiceAndType(user, service, type);

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
            tierlist.setUser(user);
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
    }
}
