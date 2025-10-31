package at.pcgamingfreaks.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ContentType {
    ANIME,
    MANGA,
    MOVIES,
    TVSHOWS,
    TVSHOWS_SEASONS;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ContentType from(String text) {
        text = text.replace("-", "_");
        for (ContentType type : ContentType.values()) {
            if (type.name().equalsIgnoreCase(text)) return type;
        }
        throw new IllegalArgumentException();
    }
}
