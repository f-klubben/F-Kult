package dk.fklub.fkult.presentation.controllers;

import dk.fklub.fkult.business.services.Authenticator;
import dk.fklub.fkult.business.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

record UsernameDTO(String username) {}

//controller for authenticator using route "/api/auth"
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final Authenticator auth;
    private final UserService user;

    public AuthController(Authenticator auth, UserService user) { 
        this.auth = auth; 
        this.user = user;
    }

    //route that logs user in by username unless banned or not exsists
    @PostMapping("/username")
    public ResponseEntity<String> sendUsername(@RequestBody UsernameDTO body) {

        //check if username exists using authenticator functions
        boolean exists = auth.receiveUsername(body.username());
        if (exists) {//if user exists, check if user is banned, if yes return banned if not login
            boolean banned = user.getIfUserBanned(body.username());
            if(!banned){
                return ResponseEntity.ok("Login successful");
            }else
            {
                return ResponseEntity.status(404).body("Your Banned Bozo!");
            }
        } else {
            // return a body so the frontend can display it
            return ResponseEntity.status(404).body("Username does not exist");
        }
    }
}




