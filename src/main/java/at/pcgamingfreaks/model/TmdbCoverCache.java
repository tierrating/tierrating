package at.pcgamingfreaks.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "id", "season"})
})public class TmdbCoverCache {
    @Id
    private long id;
    @Nullable
    private Long season;
    private String coverUrl;
}
