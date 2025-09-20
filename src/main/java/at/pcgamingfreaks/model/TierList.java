package at.pcgamingfreaks.model;

import at.pcgamingfreaks.model.auth.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity(name = "tierlists")
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user.id", "service", "type"})
})
public class TierList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @Enumerated(EnumType.STRING)
    private ThirdPartyService service;
    @Enumerated(EnumType.STRING)
    private ContentType type;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Tier> tiers;
}
