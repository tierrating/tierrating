package at.pcgamingfreaks.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericResponseDTO {
    protected boolean success;
    protected String message;
}
