package at.pcgamingfreaks.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TmdbInfoRequest {
    @JsonProperty("poster_path")
    private String posterPath;
}
