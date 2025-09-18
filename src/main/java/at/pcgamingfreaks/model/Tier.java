package at.pcgamingfreaks.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tiers")
public class Tier {

    public Tier(String color, String name, double score, double adjustedScore) {
        this(null, color, name, score, adjustedScore);
    }

    public Tier(UUID id, String color, String name, double score, double adjustedScore) {
        this.id = id;
        this.color = color;
        this.name = name;
        this.score = score;
        this.adjustedScore = adjustedScore;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    private TierList tierlist;

    private String color;
    private String name;
    private double score;
    private double adjustedScore;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tier tier = (Tier) o;
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
