package at.pcgamingfreaks.mapper;

import at.pcgamingfreaks.model.anilist.AniListListEntry;
import at.pcgamingfreaks.model.anilist.AniListMediaCoverImage;
import at.pcgamingfreaks.model.anilist.AniListMediaTitle;
import at.pcgamingfreaks.model.dto.ListEntryDTO;
import at.pcgamingfreaks.service.TmdbCoverFinder;
import com.uwetrottmann.trakt5.entities.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListEntryDtoMapper {
    private final TmdbCoverFinder tmdbCoverFinder;

    public ListEntryDTO map(AniListListEntry entry) {
        ListEntryDTO dto = new ListEntryDTO();
        dto.setId(entry.getMedia().getId());

        AniListMediaTitle title = entry.getMedia().getTitle();
        dto.setTitle(Strings.isNotBlank(title.getEnglish()) ? title.getEnglish() : title.getRomaji());

        AniListMediaCoverImage cover = entry.getMedia().getCoverImage();
        dto.setCover(Strings.isNotBlank(cover.getLarge()) ? cover.getLarge() : cover.getExtraLarge());

        dto.setScore(entry.getScore());
        return dto;
    }

    public ListEntryDTO map(Object object) {
        return new ListEntryDTO();
    }

    public ListEntryDTO map(BaseRatedEntity entry) {
        if (entry instanceof RatedMovie) return map((RatedMovie) entry);
        if (entry instanceof RatedSeason) return map((RatedSeason) entry);
        if (entry instanceof RatedShow) return map((RatedShow) entry);
        throw new RuntimeException("Invalid entry type");
    }

    public ListEntryDTO map(RatedMovie entry) {
        ListEntryDTO dto = new ListEntryDTO();
        dto.setId(entry.movie.ids.trakt);
        dto.setTitle(entry.movie.title);
        dto.setCover(tmdbCoverFinder.findMovie(entry.movie.ids.tmdb));
        dto.setScore(entry.rating.value);
        return dto;
    }

    public ListEntryDTO map(BaseMovie entry) {
        ListEntryDTO dto = new ListEntryDTO();
        dto.setId(entry.movie.ids.trakt);
        dto.setTitle(entry.movie.title);
        dto.setCover(tmdbCoverFinder.findMovie(entry.movie.ids.tmdb));
        dto.setScore(0);
        return dto;
    }

    public ListEntryDTO map(RatedShow entry) {
        ListEntryDTO dto = new ListEntryDTO();
        dto.setId(entry.show.ids.trakt);
        dto.setTitle(entry.show.title);
        dto.setCover(tmdbCoverFinder.findShow(entry.show.ids.tmdb));
        dto.setScore(entry.rating.value);
        return dto;
    }

    public ListEntryDTO map(BaseShow entry) {
        ListEntryDTO dto = new ListEntryDTO();
        dto.setId(entry.show.ids.trakt);
        dto.setTitle(entry.show.title);
        dto.setCover(tmdbCoverFinder.findShow(entry.show.ids.tmdb));
        dto.setScore(0);
        return dto;
    }

    private ListEntryDTO map(RatedSeason entry) {
        ListEntryDTO dto = new ListEntryDTO();
        dto.setId(entry.season.ids.trakt);
        dto.setTitle(String.format("%s %s", entry.show.title, entry.season.title));
        dto.setCover(tmdbCoverFinder.findSeason(entry.show.ids.tmdb, entry.season.number));
        dto.setScore(entry.rating.value);
        return dto;
    }
}
