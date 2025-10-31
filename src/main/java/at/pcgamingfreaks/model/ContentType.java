package at.pcgamingfreaks.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ContentType {
    ANIME,
    MANGA,
    MOVIES,
    TVSHOWS;

    public String toString() {
        return name().toLowerCase();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ContentType from(String text) {
        for (ContentType type : ContentType.values()) {
            if (type.toString().equalsIgnoreCase(text)) return type;
        }
        throw new IllegalArgumentException();
    }
}
