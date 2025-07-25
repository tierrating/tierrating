package at.pcgamingfreaks.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "tiers")
public class Tier {

    public Tier(String color, String name, double score, double adjustedScore) {
        this.color = color;
        this.name = name;
        this.score = score;
        this.adjustedScore = adjustedScore;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private TierList tierlist;

    private String color;
    private String name;
    private double score;
    private double adjustedScore;
}
