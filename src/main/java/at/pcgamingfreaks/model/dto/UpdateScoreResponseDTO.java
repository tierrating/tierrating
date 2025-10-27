package at.pcgamingfreaks.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateScoreResponseDTO {
    private boolean error;
    private String message;

    public static UpdateScoreResponseDTO success() {
        return new UpdateScoreResponseDTO(false, "");
    }
}
