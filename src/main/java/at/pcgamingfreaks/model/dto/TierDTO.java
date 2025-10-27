package at.pcgamingfreaks.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TierDTO {
    private UUID id;
    private String color;
    private String name;
    private double score;
    private double adjustedScore;

    public TierDTO(String color, String name, double score, double adjustedScore) {
        this(null,  color, name, score, adjustedScore);
    }

    public TierDTO(UUID id, String color, String name, double score, double adjustedScore) {
        this.id = id;
        this.color = color;
        this.name = name;
        this.score = score;
        this.adjustedScore = adjustedScore;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TierDTO tier = (TierDTO) o;
        return Double.compare(score, tier.score) == 0
                && Double.compare(adjustedScore, tier.adjustedScore) == 0
                && id.compareTo(tier.id) == 0
                && color.equals(tier.color)
                && name.equals(tier.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, color, name, score, adjustedScore);
    }
}
