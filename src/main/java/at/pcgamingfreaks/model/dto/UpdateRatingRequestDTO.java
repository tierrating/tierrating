package at.pcgamingfreaks.model.dto;

import at.pcgamingfreaks.model.ThirdPartyService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateRatingRequestDTO {
    private String username;
    private ThirdPartyService service;

    private long id;
    private double score;
}
