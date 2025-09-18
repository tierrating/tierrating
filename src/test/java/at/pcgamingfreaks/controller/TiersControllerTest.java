package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.ContentType;
import at.pcgamingfreaks.model.Service;
import at.pcgamingfreaks.model.Tier;
import at.pcgamingfreaks.model.TierList;
import at.pcgamingfreaks.model.auth.AniListConnection;
import at.pcgamingfreaks.model.auth.User;
import at.pcgamingfreaks.model.dto.TierDTO;
import at.pcgamingfreaks.model.repo.TierListsRepository;
import at.pcgamingfreaks.model.repo.TiersRepository;
import at.pcgamingfreaks.model.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class TiersControllerTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TierListsRepository tierListsRepository;
    @Mock
    private TiersRepository tiersRepository;

    @InjectMocks
    private TiersController tiersController;

    @ParameterizedTest
    @MethodSource("notFound")
    public void setTierListNotFound(Optional<User> user) {
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        ResponseEntity<?> result = tiersController.setTierlist("testing", Service.ANILIST, ContentType.ANIME, new ArrayList<>());
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatusCode().value());
    }

    private static Stream<Arguments> notFound() {
        return Stream.of(Arguments.of(Optional.empty()), Arguments.of(Optional.of(new User())));
    }

    @Test
    public void setTierListEmpty() {
        User user = new User();
        user.setUsername("test");
        user.setAnilistConnection(new AniListConnection());
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));
        when(tierListsRepository.findByUserAndServiceAndType(any(), any(), any())).thenReturn(Optional.empty());
        when(tierListsRepository.save(any())).thenReturn(null);

        List<TierDTO> tiers = List.of(
                new TierDTO("#111111", "testing 1", 10, 10),
                new TierDTO("#222222", "testing 2", 8, 8),
                new TierDTO("#333333", "testing 3", 6, 6),
                new TierDTO("#444444", "testing 4", 4, 4)
        );

        ArgumentCaptor<TierList> tierListCaptor = ArgumentCaptor.forClass(TierList.class);

        tiersController.setTierlist("test", Service.ANILIST, ContentType.ANIME, tiers);

        verify(tierListsRepository, times(1)).save(tierListCaptor.capture());
        TierList capturedTierlist = tierListCaptor.getValue();
        assertEquals(user, capturedTierlist.getUser());
        assertEquals(Service.ANILIST, capturedTierlist.getService());
        assertEquals(ContentType.ANIME, capturedTierlist.getType());
        for (int i = 0; i < capturedTierlist.getTiers().size() && i < tiers.size(); i++) {
            assertEquals(capturedTierlist.getTiers().get(i).getColor(), tiers.get(i).getColor());
            assertEquals(capturedTierlist.getTiers().get(i).getName(), tiers.get(i).getName());
            assertEquals(capturedTierlist.getTiers().get(i).getScore(), tiers.get(i).getScore());
            assertEquals(capturedTierlist.getTiers().get(i).getAdjustedScore(), tiers.get(i).getAdjustedScore());
        }
    }

    @Test
    public void setTierListExisting() {
        User user = new User();
        user.setUsername("test");
        user.setAnilistConnection(new AniListConnection());
        when(userRepository.findByUsername(any())).thenReturn(Optional.of(user));

        List<Tier> existingTiers = new ArrayList<>();
        existingTiers.add(new Tier(UUID.randomUUID(), "#666666", "testing 6", 10, 10));
        existingTiers.add(new Tier(UUID.randomUUID(), "#777777", "testing 7", 7, 7));
        existingTiers.add(new Tier(UUID.randomUUID(), "#888888", "testing 8", 4, 4));
        existingTiers.add(new Tier(UUID.randomUUID(), "#999999", "testing 9", 1, 1));

        TierList tierList = new TierList();
        tierList.setUser(user);
        tierList.setTiers(existingTiers);
        when(tierListsRepository.findByUserAndServiceAndType(any(), any(), any())).thenReturn(Optional.of(tierList));
        when(tierListsRepository.save(any())).thenReturn(null);

        List<TierDTO> tiers = List.of(
                new TierDTO("#111111", "testing 1", 10, 10),
                new TierDTO("#222222", "testing 2", 8, 8),
                new TierDTO("#333333", "testing 3", 6, 6),
                new TierDTO("#444444", "testing 4", 4, 4)
        );

        ArgumentCaptor<TierList> tierListCaptor = ArgumentCaptor.forClass(TierList.class);

        tiersController.setTierlist("test", Service.ANILIST, ContentType.ANIME, tiers);

        verify(tierListsRepository, times(1)).save(tierListCaptor.capture());
        TierList capturedTierlist = tierListCaptor.getValue();
        assertEquals(user, capturedTierlist.getUser());
        for (int i = 0; i < capturedTierlist.getTiers().size() && i < tiers.size(); i++) {
            assertEquals(capturedTierlist.getTiers().get(i).getColor(), tiers.get(i).getColor());
            assertEquals(capturedTierlist.getTiers().get(i).getName(), tiers.get(i).getName());
            assertEquals(capturedTierlist.getTiers().get(i).getScore(), tiers.get(i).getScore());
            assertEquals(capturedTierlist.getTiers().get(i).getAdjustedScore(), tiers.get(i).getAdjustedScore());
        }
    }
}