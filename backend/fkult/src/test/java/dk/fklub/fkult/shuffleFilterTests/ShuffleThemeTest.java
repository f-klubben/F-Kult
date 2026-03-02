package dk.fklub.fkult.shuffleFilterTests;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import dk.fklub.fkult.business.services.shuffleFilter.ShuffleFilter;
import dk.fklub.fkult.persistence.entities.Theme;

public class ShuffleThemeTest {
    private ShuffleFilter shuffleFilter;
    private List<Theme> theme;

    @BeforeEach
    void setUp() {
        shuffleFilter = new ShuffleFilter();
        theme = new ArrayList<>();

        theme.add(new Theme(1l, "Theme 1", 5l, null, null));
        theme.add(new Theme(5l, "Theme 2", 4l, null, null));
        theme.add(new Theme(9l, "Theme 3", 56l, null, null));
        theme.add(new Theme(61l, "Theme 4", 4l, null, null)); 
        theme.add(new Theme(98l, "Theme 5", 86l, null, null));
        theme.add(new Theme(8l, "Theme 6", 123l, null, null));
        theme.add(new Theme(3l, "Theme 7", 669l, null, null));
        theme.add(new Theme(67l, "Theme 8", 1l, null, null));
        theme.add(new Theme(73l, "Theme 9", 16l, null, null));
    }

    @Test
    void quickShuffleFilter() {
        List<Theme> shuffled = shuffleFilter.quickShuffle(theme);
        
        assertEquals(theme.size(), shuffled.size());
        assertTrue(shuffled.containsAll(theme));

        boolean sameOrder = true;
        for (int i = 0; i < theme.size(); i++) {
            if (theme.get(i) != shuffled.get(i)) {
                sameOrder = false;
                break;
            }
        }
        assertFalse(sameOrder);
    }

    @Test
    void weightedShuffleFilter() {
        List<Theme> shuffled = shuffleFilter.weightedShuffle(theme);
        
        assertEquals(theme.size(), shuffled.size());
        assertTrue(shuffled.containsAll(theme));

        boolean sameOrder = true;
        for (int i = 0; i < theme.size(); i++) {
            if (theme.get(i) != shuffled.get(i)) {
                sameOrder = false;
                break;
            }
        }
        assertFalse(sameOrder);
    }
}