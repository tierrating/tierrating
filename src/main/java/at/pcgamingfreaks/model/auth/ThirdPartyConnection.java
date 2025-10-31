package at.pcgamingfreaks.model.auth;

import at.pcgamingfreaks.model.ThirdPartyService;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "service"})
})
public class ThirdPartyConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private ThirdPartyService service;
    private String thirdpartyUserId;

    private LocalDateTime expiresOn;
    @Column(length = 2047)
    private String accessToken;
    @Column(length = 2047)
    private String refreshToken;

}
