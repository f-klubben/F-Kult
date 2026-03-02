package dk.fklub.fkult.ThemeTests;

import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import com.p3.fkult.persistence.entities.Theme;
import com.p3.fkult.persistence.repository.ThemeRepository;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.Null;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ThemeRepositoryTest {

    private ThemeRepository themeRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        themeRepository  = new ThemeRepository(jdbcTemplate);
    }

    //find all
    @Test
    void findall_returns(){
        //arrange
        Theme t1 = new Theme(1L, "themeA", 5L, LocalDateTime.now(),0);
        Theme t2 = new Theme(2L, "themeB", 6L, LocalDateTime.now(),0);
        List<Theme> expectedThemes = List.of(t1, t2);

        when(jdbcTemplate.query(eq("SELECT * FROM theme"), any(RowMapper.class))).thenReturn(List.of(t1,t2));

        //act
        List<Theme> result = themeRepository.findAll();

        //assert
        assertThat(result).containsExactly(t1,t2);
        verify(jdbcTemplate).query(eq("SELECT * FROM theme"), any(RowMapper.class));
    }


    //find by ID/*
    @Test
    void findById_returnsTheme(){
        //arrange
        Theme theme = new Theme(1L, "test", 5L, LocalDateTime.now(),0);

        when(jdbcTemplate.queryForObject(eq("SELECT * FROM theme WHERE id = ?"), any(RowMapper.class), eq(1L))).thenReturn(theme);

        //act
        Theme result = themeRepository.findById(1L);

        //assert
        assertThat(result).isEqualTo(theme);

    }

    @Test
    void findById_returnsNull_notFound (){
        //arrange
        when(jdbcTemplate.queryForObject(any(), any(RowMapper.class), any())).thenThrow(new EmptyResultDataAccessException(1));

        //act
        Theme result = themeRepository.findById(125L);

        //assert
        assertThat(result).isNull();

    }


    //find after
    @Test
    void findAfter_returnsThemes(){
        // Arrange
        LocalDateTime dt = LocalDateTime.now();
        Theme t = new Theme(1L, "Test", 5L, dt.plusHours(1), 2);

        when(jdbcTemplate.query(eq("SELECT * FROM theme WHERE timestamp >= ?"),any(RowMapper.class), eq(dt))).thenReturn(List.of(t));

        // Act
        List<Theme> result = themeRepository.findAfter(dt);

        // Assert
        assertThat(result).containsExactly(t);
    }
    //findafter handles emptylist
    @Test
    void findAfter_handlesEmptyList() {
        // Arrange
        LocalDateTime dt = LocalDateTime.now();

        when(jdbcTemplate.query(eq("SELECT * FROM theme WHERE timestamp >= ?"), any(RowMapper.class), eq(dt))).thenReturn(List.of());

        // Act
        List<Theme> result = themeRepository.findAfter(dt);

        // Assert
        assertThat(result).isEmpty();
    }
    //findafter handles null
    @Test
    void findAfter_handlesNullList() {
        // Arrange
        LocalDateTime dt = LocalDateTime.now();

        when(jdbcTemplate.query(eq("SELECT * FROM theme WHERE timestamp >= ?"), any(RowMapper.class), eq(dt))).thenReturn(null);

        // Act
        List<Theme> result = themeRepository.findAfter(dt);

        // Assert
        assertThat(result).isNull();
    }

    
    //find before
    @Test
    void findBefore_returnsThemes() {
        // Arrange
        LocalDateTime dt = LocalDateTime.now();
        Theme t1 = new Theme(1L, "Before1", 5L, dt.minusDays(1), 0);
        Theme t2 = new Theme(2L, "Before2", 6L, dt.minusHours(1), 0);

        when(jdbcTemplate.query(eq("SELECT * FROM theme WHERE timestamp < ?"), any(RowMapper.class), eq(dt))).thenReturn(List.of(t1, t2));

        // Act
        List<Theme> result = themeRepository.findBefore(dt);

        // Assert
        assertThat(result).containsExactly(t1, t2);
        verify(jdbcTemplate).query(eq("SELECT * FROM theme WHERE timestamp < ?"), any(RowMapper.class), eq(dt));
    }


    // save
    @Test
    void save_insertsThemeAndReturnsWithId() {
        // Arrange
        Theme input = new Theme(0L, "TestTheme", 99L, LocalDateTime.now(),null);

        when(jdbcTemplate.queryForObject("SELECT last_insert_rowid()", Long.class)).thenReturn(42L);
    

        // Act
        Theme result = themeRepository.save(input);

        // Assert
        ArgumentCaptor<Object[]> captor = ArgumentCaptor.forClass(Object[].class);

        verify(jdbcTemplate).update(eq("INSERT INTO theme (name, user_id) VALUES (?,?)"),eq("TestTheme"), eq(99L));
        
        assertThat(result.getId()).isEqualTo(42L);
    }

    // delete
    @Test
    void delete_executesCorrectSQL() {
        // Arrange
        long id = 55L;

        // Act
        themeRepository.delete(id);

        // Assert
        verify(jdbcTemplate).update("DELETE FROM theme WHERE id = ?", id);
    }

    // SET VOTES
    @Test
    void setVote_updatesVoteCount() {
        // Arrange
        long id = 1L;
        long votes = 10L;

        // Act
        themeRepository.setVote(id, votes);

        // Assert
        verify(jdbcTemplate).update("UPDATE theme SET vote_count = (?) WHERE id = (?)", votes, id);
    }

    // find from user
    @Test
    void findFromUser_returnsThemesForGivenUser() {
        // Arrange
        Long userId = 99L;
        Theme t1 = new Theme(1L, "UserTheme1", userId, LocalDateTime.now(), null);
        Theme t2 = new Theme(2L, "UserTheme2", userId, LocalDateTime.now(), null);

        when(jdbcTemplate.query(eq("SELECT * FROM theme WHERE user_id = ? AND vote_count IS NULL"), any(RowMapper.class), eq(userId))).thenReturn(List.of(t1, t2));

        // Act
        List<Theme> result = themeRepository.findFromUser(userId);

        // Assert
        assertThat(result).containsExactly(t1, t2);
        verify(jdbcTemplate).query( eq("SELECT * FROM theme WHERE user_id = ? AND vote_count IS NULL"), any(RowMapper.class), eq(userId));
    }


    // update name
    @Test
    void updateName_updatesThemeName() {
        // Arrange
        long id = 5L;
        String newName = "New Theme Name";

        // Act
        themeRepository.updateName(id, newName);

        // Assert
        verify(jdbcTemplate).update("UPDATE theme SET name = ? WHERE id = ?", newName, id);
    }

    //rowmapper mapping test
    @Test
    void rowMapper_mapsResultSetCorrectly() throws Exception {
        // Arrange
        RowMapper<Theme> mapper = themeRepository.getRowMapper();

        ResultSet rs = mock(ResultSet.class);

        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("name")).thenReturn("Mapped");
        when(rs.getLong("user_id")).thenReturn(22L);

        LocalDateTime ts = LocalDateTime.now();
        Timestamp dbTs = Timestamp.valueOf(ts);

        when(rs.getTimestamp("timestamp")).thenReturn(dbTs);
        when(rs.getObject("vote_count")).thenReturn(5);

        // Act
        Theme result = mapper.mapRow(rs, 0);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Mapped");
        assertThat(result.getUserid()).isEqualTo(22L);
        assertThat(result.getTimestamp()).isEqualTo(ts.plusHours(1));
        assertThat(result.getVotecount()).isEqualTo(5);
    }

    // find votes by id
    @Test
    void findVotesById_returnsVoteCount() {
        // Arrange
        long id = 5L;
        long expectedVotes = 12L;

        when(jdbcTemplate.queryForObject("SELECT vote_count FROM theme WHERE id = ?", Long.class, id)).thenReturn(expectedVotes);

        // Act
        Long result = themeRepository.findVotesById(id);

        // Assert
        assertThat(result).isEqualTo(expectedVotes);
        verify(jdbcTemplate).queryForObject("SELECT vote_count FROM theme WHERE id = ?", Long.class, id);
    }
}