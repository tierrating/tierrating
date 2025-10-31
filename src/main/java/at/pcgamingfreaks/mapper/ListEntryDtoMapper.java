package at.pcgamingfreaks.mapper;

import at.pcgamingfreaks.model.anilist.AniListListEntry;
import at.pcgamingfreaks.model.anilist.AniListMediaCoverImage;
import at.pcgamingfreaks.model.anilist.AniListMediaTitle;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import com.uwetrottmann.trakt5.entities.BaseRatedEntity;
import com.uwetrottmann.trakt5.entities.RatedMovie;
import com.uwetrottmann.trakt5.entities.RatedShow;
import org.apache.logging.log4j.util.Strings;

public class ListEntryDtoMapper {
    public static ListEntryDTO map(AniListListEntry entry) {
        ListEntryDTO dto = new ListEntryDTO();
        dto.setId(entry.getMedia().getId());

        AniListMediaTitle title = entry.getMedia().getTitle();
        dto.setTitle(Strings.isNotBlank(title.getEnglish()) ? title.getEnglish() : title.getRomaji());

        AniListMediaCoverImage cover = entry.getMedia().getCoverImage();
        dto.setCover(Strings.isNotBlank(cover.getLarge()) ? cover.getLarge() : cover.getExtraLarge());

        dto.setScore(entry.getScore());
        return dto;
    }

    public static ListEntryDTO map(BaseRatedEntity entry) {
        if (entry instanceof RatedMovie) return map((RatedMovie) entry);
        if (entry instanceof RatedShow) return map((RatedShow) entry);
        throw new RuntimeException("Invalid entry type");
    }

    private static ListEntryDTO map(RatedMovie entry) {
        ListEntryDTO dto = new ListEntryDTO();
        dto.setId(entry.movie.ids.trakt);
        dto.setTitle(entry.movie.title);
        dto.setCover("https://walter-r2.trakt.tv/images/shows/000/169/967/posters/thumb/cdeb60fec1.jpg.webp");
        dto.setScore(entry.rating.value);
        return dto;
    }

    private static ListEntryDTO map(RatedShow entry) {
        ListEntryDTO dto = new ListEntryDTO();
        dto.setId(entry.show.ids.trakt);
        dto.setTitle(entry.show.title);
        dto.setCover("https://walter-r2.trakt.tv/images/shows/000/169/967/posters/thumb/cdeb60fec1.jpg.webp");
        dto.setScore(entry.rating.value);
        return dto;
    }
}
