package at.pcgamingfreaks.model.dto;

public class AccountDeletionResponseDTO extends GenericResponseDTO {
    public AccountDeletionResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
