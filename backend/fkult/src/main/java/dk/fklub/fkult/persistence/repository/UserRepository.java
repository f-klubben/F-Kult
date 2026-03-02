package dk.fklub.fkult.persistence.repository;

import dk.fklub.fkult.persistence.entities.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    // Constructor :D
    public UserRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> rowMapper = (rs, rowNum) ->
            new User(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getInt("is_banned"),
                    rs.getInt("is_admin")
            );

    // Database operations
    // Get all users
    public List<User> findAll(){
        return jdbcTemplate.query("SELECT * FROM user", rowMapper);
    }

    // Get specific user from username
    public User findUser(String username){
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM user WHERE username = ?", rowMapper, username);
        } catch (EmptyResultDataAccessException e){
            return new User(-1, "error", "Error Error", 0, 0);
        }
    }

    // Update the admin value of a user. 1 = admin, 0 = normal
    public User updateAdminStatus(String username, int status){
        jdbcTemplate.update("UPDATE user SET is_admin = ? WHERE username = ?", status, username);
        return jdbcTemplate.queryForObject("SELECT * FROM user WHERE username = ?", rowMapper, username);
    }

    // Update the ban value of a user. 1 = banned, 0 = normal
    public User updateUserBanStatus(String username, int status){
        jdbcTemplate.update("UPDATE user SET is_banned = ? WHERE username = ?", status, username);
        return jdbcTemplate.queryForObject("SELECT * FROM user WHERE username = ?", rowMapper, username);
    }

    // Find ID by username
    public long findIdByUsername(String username){
        return jdbcTemplate.queryForObject("SELECT id FROM user WHERE username = ?", Long.class, username);
    }

    // Check if user banned
    public boolean findIfUserBanned(String username){
        return jdbcTemplate.queryForObject("SELECT is_banned FROM user WHERE username = ?", boolean.class, username);
    }

    // Find a users name from their userid
    public String findUserNameById(Long id){
        return jdbcTemplate.queryForObject("SELECT name FROM user WHERE id = ?", String.class, id);
    }

}
