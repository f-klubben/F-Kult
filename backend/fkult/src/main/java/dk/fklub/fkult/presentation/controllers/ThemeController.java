package dk.fklub.fkult.presentation.controllers;

import dk.fklub.fkult.business.services.ThemeService;
import dk.fklub.fkult.business.services.UserService;
import dk.fklub.fkult.presentation.DTOs.ThemeRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/themes")
public class ThemeController {

    private final ThemeService themeService;
    private final UserService userService;

    public ThemeController(ThemeService themeService, UserService userService) {
        this.themeService = themeService;
        this.userService = userService;
    }

    @GetMapping
    public List<ThemeRequest> getThemes(){
        return themeService.getAllThemes();
    }

    @GetMapping("/New")
    public List<ThemeRequest> getNewThemes(){
        return themeService.getNewThemes();
    }
    @GetMapping("/Old")
    public List<ThemeRequest> getOldThemes(){
        return themeService.getOldThemes();
    }
    @GetMapping("/User")
    public List<ThemeRequest> getUserThemes(@RequestParam String username){
        return themeService.getUserThemes(username);
    }

    @PostMapping
    public ResponseEntity<String> createTheme(@RequestBody ThemeRequest themeRequest){//RequestBody means we parse JSON user sent to a DTO object
        String name = themeRequest.getName();
        String username = themeRequest.getUsername();
        List<String> tConsts = themeRequest.gettConsts();
        List<String> drinkingRules = themeRequest.getDrinkingRules();
        //save some variables and input validate if they are correct
        if (name == null || name.trim().isBlank() || tConsts.isEmpty() ||username == null || username.trim().isEmpty())
        {
            return ResponseEntity.badRequest().body("Theme data not accepted please ensure there is a title, username and at least one movie");
        }
        if (userService.getIfUserBanned(username)){
            return ResponseEntity.badRequest().body("Theme data not accepted as user is banned");
        }
        themeService.createThemeWithTConsts(themeRequest);
        return ResponseEntity.ok("Theme created successfully");
    }

    //function to edit themes
    @PutMapping("/{id}")
    public ResponseEntity<String> updateTheme(@PathVariable long id, @RequestBody ThemeRequest themeRequest) {
        themeRequest.setThemeId(id); // make sure themeId matches URL
        System.out.println("UpdateTheme request: " + themeRequest);

        //make sure edit values are not empty: name and movies (tconsts)
        if (themeRequest.getName() == null || themeRequest.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Name cannot be empty.");
        }
        if (themeRequest.gettConsts() == null || themeRequest.gettConsts().isEmpty()) {
            return ResponseEntity.badRequest().body("Theme must have at least 1 movie.");
        }

        //use function to update theme with tconst
        themeService.updateThemeWithTConsts(themeRequest);
        return ResponseEntity.ok("Theme updated successfully.");
    }
}