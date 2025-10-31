package at.pcgamingfreaks.mapper;

import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.UserDTO;


public class UserDtoMapper {
    public static UserDTO map(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setBio(user.getBio() == null ? "" : user.getBio());
        dto.setAnilistConnected(user.getAnilistConnection() != null);
        dto.setTraktConnected(user.getTraktConnection() != null);
        return dto;
    }
}
