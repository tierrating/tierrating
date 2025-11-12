package at.pcgamingfreaks.model.dto;

public class ThirdPartyRemovalResponseDTO extends GenericResponseDTO {
    public ThirdPartyRemovalResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
