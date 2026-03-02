package dk.fklub.fkult.business.services;

import dk.fklub.fkult.persistence.entities.User;
import dk.fklub.fkult.persistence.repository.AuthRepository;
import dk.fklub.fkult.persistence.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final Authenticator auth;

    public UserService(UserRepository userRepository, Authenticator auth) {
        this.userRepository = userRepository;
        this.auth = auth;
    }

    // Get all the users
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    // Get a user from username
    public User getUser(String username){
        return userRepository.findUser(username);
    }

    // Update admin value of user
    public ResponseEntity<?> postAdminUser(String username, int status){
        // Check if status is 1 or 0
        if (status > 1 || status < 0) return ResponseEntity.status(400).body("Status not applicable: " + status);

        // Make sure user exists
        boolean res = auth.receiveUsername(username);
        if (!res) return ResponseEntity.status(403).body("User does not exist");

        // Update the value of the user and check if the user was properly updated
        User result = userRepository.updateAdminStatus(username, status);
        if (result.getAdmin() == status) return ResponseEntity.ok("User successfully " + ((status == 1) ? "admin" : "unadmin"));
        else return ResponseEntity.status(500).body("User not correctly updated");
    }

    // Ban or unban user
    public ResponseEntity<?> postUserBan(String username, int status){
        // Check if user exists
        boolean res = auth.receiveUsername(username);
        if (!res) return ResponseEntity.status(403).body("User does not exist");

        // Check if user is already banned or unbanned
        if (getIfUserBanned(username) == (status == 1)) return ResponseEntity.ok("User already " + ((status == 1) ? "banned" : "unbanned"));

        // Update ban value and check if the operation happened properly
        User result = userRepository.updateUserBanStatus(username, status);
        if (result.getBanned() == status) return ResponseEntity.ok("User successfully " + ((status == 1) ? "banned" : "unbanned"));
        else return ResponseEntity.status(500).body("User not correctly updated");
    }

    // Find userid by username
    public long getUserIdByUsername(String username){
        return userRepository.findIdByUsername(username);
    }

    // Check if a user is banned
    public boolean getIfUserBanned(String username){
        return userRepository.findIfUserBanned(username);
    }

    // Get a user's full name by their userid
    public String getUserNameById(long id){
        // Get the name
        String name = userRepository.findUserNameById(id);
        // Check if the name was found
        if (name != null){
            return name;
        }
        return "User not found";
    }

}