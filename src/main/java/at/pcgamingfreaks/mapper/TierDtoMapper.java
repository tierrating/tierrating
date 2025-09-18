package at.pcgamingfreaks.mapper;

import at.pcgamingfreaks.model.Tier;
import at.pcgamingfreaks.model.dto.TierDTO;

public class TierDtoMapper {
    public static TierDTO map(Tier tier) {
        TierDTO dto = new TierDTO();
        dto.setId(tier.getId());
        dto.setName(tier.getName());
        dto.setColor(tier.getColor());
        dto.setScore(tier.getScore());
        dto.setAdjustedScore(tier.getAdjustedScore());
        return dto;
    }

    /**
     * Setting data from dto to a new {@link Tier} object. {@link Tier#id} is not set, because this is handled by JPA.
     */
    public static Tier map(TierDTO dto) {
        Tier tier = new Tier();
        tier.setName(dto.getName());
        tier.setColor(dto.getColor());
        tier.setScore(dto.getScore());
        tier.setAdjustedScore(dto.getAdjustedScore());
        return tier;
    }
}
