package at.pcgamingfreaks.model.dto;

public class ChangePasswordResponseDTO extends GenericResponseDTO {
    public ChangePasswordResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
