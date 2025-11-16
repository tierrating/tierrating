package at.pcgamingfreaks.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ListEntryDTO {
    private long id;
    private String title;
    private String cover;

    private float score;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ListEntryDTO that = (ListEntryDTO) o;
        return id == that.id && Float.compare(score, that.score) == 0 && Objects.equals(title, that.title) && Objects.equals(cover, that.cover);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, cover, score);
    }
}
