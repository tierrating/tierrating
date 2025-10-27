package at.pcgamingfreaks.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListEntryDTO {
    private long id;
    private String title;
    private String cover;

    private float score;
}
