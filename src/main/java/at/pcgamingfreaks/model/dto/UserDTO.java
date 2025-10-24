package at.pcgamingfreaks.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private long id;

    private String username;
    private String bio;

    private boolean isAnilistConnected;
    private boolean isTraktConnected;
}