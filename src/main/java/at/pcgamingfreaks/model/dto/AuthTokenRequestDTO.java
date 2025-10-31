package at.pcgamingfreaks.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthTokenRequestDTO {
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    @JsonProperty("redirect_uri")
    private String redirectUri;
    @JsonProperty("grant_type")
    private String grantType;
    private String code;
}
