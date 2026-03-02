package dk.fklub.fkult.persistence.repository;

import dk.fklub.fkult.persistence.entities.Theme;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    //Spring Boot auto-injects the jdbcTemplate
    public ThemeRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }


    private final RowMapper<Theme> rowMapper = (rs, rowNum) ->
            new Theme(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getLong("user_id"),
                    rs.getTimestamp("timestamp").toLocalDateTime().plusHours(1),

                    // Handle possible null for vote_count
                    rs.getObject("vote_count") == null 
                        ? null 
                        : ((Number) rs.getObject("vote_count")).intValue()
            );
    //for testing purposes
    public RowMapper<Theme> getRowMapper() {
    return rowMapper;
}
    //database operations
    public List<Theme> findAll(){
        return jdbcTemplate.query("SELECT * FROM theme", rowMapper);
    }

    public Theme findById(Long id){
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM theme WHERE id = ?",rowMapper, id);
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    public List<Theme> findAfter(LocalDateTime localDate){
        //timestamp >= localDate finds themes created on or after localDate
        //newer timestamps have larger values
        List<Theme> themes = jdbcTemplate.query("SELECT * FROM theme WHERE timestamp >= ?", rowMapper, localDate);
        if (themes == null || themes.isEmpty()){
            System.out.println("failed to get themes from db");
        }else{
            System.out.println("Found requested themes: " +
                    //A stream is a way to process a collection. chained with .map it converts themes to strings
                    themes.stream()
                            //Theme::toString is a method reference instead of, (theme -> theme.toString)
                            .map(Theme::toString)
                            .collect(Collectors.joining(", "))
            );
        }
        return themes;
    }
    public List<Theme> findBefore(LocalDateTime localDate){
        //timestamp < localDate finds themes created before localDate
        return jdbcTemplate.query("SELECT * FROM theme WHERE timestamp < ?", rowMapper, localDate);
    }

    public List<Theme> findFromUser(Long userId){
        String sql = "SELECT * FROM theme WHERE user_id = ? AND vote_count IS NULL";
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    public Theme save(Theme theme) {
        jdbcTemplate.update("INSERT INTO theme (name, user_id) VALUES (?,?)", theme.getName(), theme.getUserid());
        // Get the last inserted ID
        Long id = jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class);
        theme.setId(id);
        return theme;
    }

    // Updates the votes of a theme based on the id
    public void setVote(long id, long votes) {
        jdbcTemplate.update("UPDATE theme SET vote_count = (?) WHERE id = (?)", votes, id);
    }

    // Deletes a theme based on the id
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
    }

    // Update theme name
    public void updateName(long id, String name) {
        jdbcTemplate.update(
            "UPDATE theme SET name = ? WHERE id = ?",
            name, id
        );
    }

    // Find votes for a theme based on the id
    public Long findVotesById(Long id) {
        return jdbcTemplate.queryForObject("SELECT vote_count FROM theme WHERE id = ?", Long.class, id);
    }
}