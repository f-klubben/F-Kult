package dk.fklub.fkult.presentation.controllers;

import dk.fklub.fkult.business.services.UserService;
import dk.fklub.fkult.persistence.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //GET all users
    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    //GET singular user by username, check for admin.
    @GetMapping("/admin/{username}")
    public int checkForAdminUser(@PathVariable String username) {
        User user = userService.getUser(username);
        return user.getAdmin();
    }

    // POST new admin value for a user
    @PostMapping("/admin/{username}")
    public ResponseEntity<?> changeAdminValueOfUser(@PathVariable String username, @RequestParam String newAdmin, @RequestParam int status) { // Username=caller, newAdmin=new user, status=admin or unadmin
        //Make sure the one sending the call is admin
        if (checkForAdminUser(username) == 0) return ResponseEntity.status(403).body("User not admin");
        //Make sure topholt and root aren't trying to be unadminned
        if (status == 0 && (Objects.equals(newAdmin, "topholt") || Objects.equals(newAdmin, "root") || Objects.equals(newAdmin, username))) return ResponseEntity.status(403).body("User can not be unadmin");

        return userService.postAdminUser(newAdmin, status);
    }

    // POST ban a user
    @PostMapping("/admin/ban_user")
    public ResponseEntity<?> banUser(@RequestBody List<String> body){
        if (body == null || body.get(0) == null || body.get(1) == null) return ResponseEntity.badRequest().build();
        if (checkForAdminUser(body.getFirst()) == 0) return ResponseEntity.status(403).body("User not admin");

        return userService.postUserBan(body.get(1), 1);
    }

    // POST unban a user
    @PostMapping("/admin/unban_user")
    public ResponseEntity<?> unbanUser(@RequestBody List<String> body){
        if (body == null || body.get(0) == null || body.get(1) == null) return ResponseEntity.badRequest().build();
        if (checkForAdminUser(body.getFirst()) == 0) return ResponseEntity.status(403).body("User not admin");

        return userService.postUserBan(body.get(1), 0);
    }

    //GET userid by their username
    @GetMapping("/id/{username}")
    public long getUserIdByUsername(@PathVariable String username){
        return userService.getUserIdByUsername(username);
    }

    //GET full name by id
    @GetMapping("/full_name/{id}")
    public String getUserNameById(@PathVariable long id){
        return userService.getUserNameById(id);
    }
}
