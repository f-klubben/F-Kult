package dk.fklub.fkult.business.services;

import dk.fklub.fkult.persistence.repository.AuthRepository;
import org.springframework.stereotype.Service;

@Service
public class Authenticator {

    //variable for authentication repository functions
    private final AuthRepository authRepo;

    public Authenticator(AuthRepository authRepo) {
        this.authRepo = authRepo;
    }

    //return true if user is found or inserted, otherwise return false
    public boolean receiveUsername(String username) {
        System.out.println("Authenticator got username: " + username);

        //check if user exists locally
        if (authRepo.userExistsLocally(username)) return true;

        //if user does not exist locally, check if memberid exists of api call
        Integer memberId = authRepo.fetchMemberId(username);
        if (memberId == null) return false;

        //if id exists, check if member data exists through api call (can happen, but very specific and rare cases)
        AuthRepository.MemberInfo info = authRepo.fetchMemberInfo(memberId);
        if (info == null) return false;

        //return username and info
        return upsertLocalUser(info, username);
    }

    // Ensures the entire method runs within a database transaction.
    // If an exception occurs, the operation will be rolled back.
    @org.springframework.transaction.annotation.Transactional
    protected boolean upsertLocalUser(AuthRepository.MemberInfo memberInfo, String fallback) {

        // Determine which username to use:
        // - Use memberInfo.username if it exists and is not blank
        // - Otherwise, fall back to the provided fallback value
        String user = (memberInfo.username != null && !memberInfo.username.isBlank()) ? memberInfo.username : fallback;

        //get memberinfo username
        String name = memberInfo.name;

        //perform upsert, and return the number of afftected rows
        int rows = authRepo.upsertUser(name, user);

        //if exactly 1 row was edited return true
        return rows == 1;
    }
}






