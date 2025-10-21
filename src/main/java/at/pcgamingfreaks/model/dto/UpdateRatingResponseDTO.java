package at.pcgamingfreaks.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateRatingResponseDTO {
    private boolean error;
    private String message;

    public static UpdateRatingResponseDTO success() {
        return new UpdateRatingResponseDTO(false, "");
    }
}
