package dk.fklub.fkult.shuffleFilterTests;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import dk.fklub.fkult.business.services.shuffleFilter.ShuffleFilter;
import dk.fklub.fkult.persistence.entities.SoundSample;

public class ShuffleSoundSampleTest {
    private ShuffleFilter shuffleFilter;
    private List<SoundSample> soundSample;

    @BeforeEach
    void setUp() {
        shuffleFilter = new ShuffleFilter();
        soundSample = new ArrayList<>();
        
        soundSample.add(new SoundSample(null, "/film/starwars", 1L));
        soundSample.add(new SoundSample("https://cdn.example.com/sound/wind.mp3", null, 1L));
        soundSample.add(new SoundSample("https://www.example.com/audio/intro.mp3", null, 2L));
        soundSample.add(new SoundSample(null, "/nature/rain", 3L));
        soundSample.add(new SoundSample("https://cdn.example.com/sound/birds.mp3", null, 3L));
        soundSample.add(new SoundSample(null, "/music/rock_guitar", 2L));
        soundSample.add(new SoundSample(null, "/studio/drums", 4L));
        soundSample.add(new SoundSample(null, "/mix/testtrack", 4L));
        soundSample.add(new SoundSample("https://sound.example.com/mixdown/final", null, 3L));
        soundSample.add(new SoundSample("https://www.example.com/effects/laser.mp3", null, 3L));
        soundSample.add(new SoundSample(null, "/ambient/forest", 6L));
    }

    @Test
    void quickShuffleFilter() {
        List<SoundSample> shuffled = shuffleFilter.quickShuffle(soundSample);

        assertEquals(soundSample.size(), shuffled.size());
        assertTrue(shuffled.containsAll(soundSample));

        boolean sameOrder = true;
        for (int i = 0; i < soundSample.size(); i++) {
            if (soundSample.get(i) != shuffled.get(i)) {
                sameOrder = false;
                break;
            }
        }
        assertFalse(sameOrder);
    }

    @Test
    void weightedShuffleFilter() {
        List<SoundSample> shuffled = shuffleFilter.weightedShuffle(soundSample);
        
        assertEquals(soundSample.size(), shuffled.size());
        assertTrue(shuffled.containsAll(soundSample));

        boolean sameOrder = true;
        for (int i = 0; i < soundSample.size(); i++) {
            if (soundSample.get(i) != shuffled.get(i)) {
                sameOrder = false;
                break;
            }
        }
        assertFalse(sameOrder);
    }

}