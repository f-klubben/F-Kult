package dk.fklub.fkult.presentation.controllers;

import dk.fklub.fkult.business.services.ThemeVotingService;
import dk.fklub.fkult.presentation.DTOs.ThemeVotingRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/vote")
public class ThemeVotingController {

    // Download copy of ThemeVotingService to use its functions
    private final ThemeVotingService themeVotingService;
    public ThemeVotingController(ThemeVotingService themeVotingService) {
        this.themeVotingService = themeVotingService;
    }

    // Get all theme data
    @GetMapping("/getShuffledThemes")
    public List<ThemeVotingRequest> getShuffledThemes() {
        return themeVotingService.getShuffledThemes();
    }

    // Update the votes on a theme
    @GetMapping("/update-vote/{id}/{votes}")
    public String updateVotes(@PathVariable("id") long id, @PathVariable("votes") long votes) {
        return themeVotingService.UpdateVote(id, votes);
    }

    // Delete a theme by its ID
    @DeleteMapping("/delete-theme/{id}")
    public String deleteTheme(@PathVariable("id") long id) {
        return themeVotingService.DeleteTheme(id);
    }

    // Modify theme such that it only has the mentioned movie, and add it to current event
    @RequestMapping("/add-wheel-winner/{winningThemeId}/{movieId}")
    public String addWheelWinner(@PathVariable long winningThemeId, @PathVariable Long movieId) {
        return themeVotingService.AddWheelWinner(winningThemeId, movieId);
    }
}