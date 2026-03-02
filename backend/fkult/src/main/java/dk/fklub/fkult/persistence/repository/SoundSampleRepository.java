package dk.fklub.fkult.persistence.repository;

import dk.fklub.fkult.persistence.entities.SoundSample;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SoundSampleRepository {

    // Setup template for database operations
    private final JdbcTemplate jdbcTemplate;

    public SoundSampleRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    // Setup RowMapper to map database rows to SoundSample objects
    private final RowMapper<SoundSample> rowMapper = (rs, rowNum) -> {
        SoundSample sample = new SoundSample(
            rs.getString("link"),
            rs.getString("file_path"),
            rs.getLong("user_id")
        );
        sample.setId(rs.getLong("id"));
        return sample;
    };

    // Get all SoundSamples from the database
    public List<SoundSample> getAll(){
        String sql = "SELECT * FROM sound_samples";
        return jdbcTemplate.query(sql, rowMapper);
    }

    // Save a SoundSample to the database
    public void save(SoundSample soundSample){
        if (soundSample.getLink() != null && soundSample.getLink().isEmpty()) {
            soundSample.setLink(null);
        }
        if (soundSample.getFilePath() != null && soundSample.getFilePath().isEmpty()) {
            soundSample.setFilePath(null);
        }

        System.out.println(">>> REPO SAVE, filePath = " + soundSample.getFilePath());

        String sql = "INSERT INTO sound_samples (link, file_path, user_id) VALUES (?,?,?)";
        jdbcTemplate.update(sql,
            soundSample.getLink(),
            soundSample.getFilePath(),
            soundSample.getUserId()
        );
    }

    // Delete a SoundSample from the database using id and link or filepath
    public String delete(String link, String filePath, String id){
        //check if link exists, if not set to null
        if (link != null && link.isEmpty()) link = null;

        //check if filepath exists, if not set to null
        if (filePath != null && filePath.isEmpty()) filePath = null;

        if (link == null && filePath == null){//if both is null, return no file/link to delete
            return "No link or file path provided for deletion.";
        } else if (link != null && filePath != null){//if both file and link is not null, demand only one of the two
            return "Please provide either link or file path for deletion, not both.";
        } else if (link != null){//if link is not null, delete from the db and update
            String sql = "DELETE FROM sound_samples WHERE link = ? AND id = ?";
            jdbcTemplate.update(sql, link, id);
            return "SoundSample with link " + link + " deleted.";
        }

        //else delete file by filepath
        String sql = "DELETE FROM sound_samples WHERE file_path = ? AND id = ?";
        jdbcTemplate.update(sql, filePath, id);
        return "SoundSample with file path " + filePath + " deleted.";
    }
}
