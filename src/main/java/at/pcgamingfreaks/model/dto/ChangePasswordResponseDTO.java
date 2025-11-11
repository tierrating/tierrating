package at.pcgamingfreaks.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChangePasswordResponseDTO {
    private boolean success;
    private String message;
}
