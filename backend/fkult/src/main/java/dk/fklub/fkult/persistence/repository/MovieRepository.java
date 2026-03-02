package dk.fklub.fkult.persistence.repository;

import dk.fklub.fkult.persistence.entities.Theme;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import dk.fklub.fkult.persistence.entities.Movie;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.io.File;

@Repository
public class MovieRepository {

    private final JdbcTemplate jdbcTemplate;

    //Spring Boot auto-injects the jdbcTemplate
    public MovieRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Movie> rowMapper = (rs, rowNum) ->
            new Movie(
                    rs.getLong("id"),
                    rs.getString("tconst"),
                    rs.getString("movie_name"),
                    rs.getString("original_movie_name"),
                    rs.getInt("year"),
                    rs.getInt("runtime_minutes"),
                    rs.getBoolean("is_active"),
                    rs.getBoolean("is_series"),
                    rs.getBoolean("is_shorts"),
                    rs.getString("poster_url"),
                    rs.getString("rating")
            );

private String sanitizeFTS(String input) {
    if (input == null) return "";

    // remove all symbols that break FTS or regex
    String sanitized = input.replaceAll("[^a-zA-Z0-9]", " ");

    // Collapse spaces
    sanitized = sanitized.replaceAll("\\s+", " ").trim();

    // if nothing alphanumeric is left return empty
    if (sanitized.isEmpty()) return "";

    return sanitized;
}

    //database operations
    public List<Movie> findAll(){
        return jdbcTemplate.query("SELECT * FROM movie", rowMapper);
    }

    public Movie findById(long id){
        String sql = "SELECT * FROM movie WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null; // or throw new RuntimeException("Movie not found with id: " + id);
        }
    }

    public Movie findByTconst(String tConst){
        String sql = "SELECT * FROM movie WHERE tconst = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, tConst);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Long findIdByName(String name){
        return jdbcTemplate.queryForObject("SELECT id FROM movie WHERE movie_name = ?", Long.class, name);
    }

    public String findNameById(Long id){
        return jdbcTemplate.queryForObject("SELECT movie_name FROM movie WHERE id = ?", String.class, id);
    }

    public String findPosterById(Long id){
        return jdbcTemplate.queryForObject("SELECT poster_url FROM movie WHERE id = ?", String.class, id);
    }

    public Double findRatingById(Long id){
        return jdbcTemplate.queryForObject("SELECT rating FROM movie WHERE id = ?", Double.class, id);
    }

    public Long findRunTimeById(Long id){
        return jdbcTemplate.queryForObject("SELECT runtime_minutes FROM movie WHERE id = ?", Long.class, id);
    }

    public void updatePosterURL(Long movieId, String posterURL){
        String sql = "UPDATE movie SET poster_url = ? WHERE id = ?";
        jdbcTemplate.update(sql, posterURL, movieId);
    }
    public List<Movie> searchMovies(String keyword, int page, int limit, String sortBy, String direction, Boolean movie, Boolean series, Boolean shorts, Boolean rated ) {
        // If search is an IMDb tconst then return exact match
        if (keyword != null && keyword.matches("tt\\d{7,8}")) {
            Movie m = findByTconst(keyword);
            return (m != null) ? List.of(m) : List.of();
        }
        // Pagination
        int offset = (page - 1) * limit;
        // Prepare safe FTS query (prefix search)
        String safe = sanitizeFTS(keyword);
        if (safe.isBlank()) {
            // User typed only garbage like "%%%..."; just return no results
            return List.of();
        }
        String likePattern = safe + "*"; // FTS5 prefix search

        String column;

        switch (sortBy == null ? "" : sortBy) {
            case "year":        column = "m.year"; break;
            case "runtime":     column = "m.runtime_minutes"; break;
            case "alphabetical":column = "m.movie_name"; break;
            case "rating":      column = "m.rating"; break;
            default:            column = "m.rating"; break;
        }
        // Validate ASC / DESC sorting direction
        String dir = (direction != null && direction.equalsIgnoreCase("asc")) ? "ASC" : "DESC";

        // Build full ORDER BY clause
        String sortColumn = column + " " + dir;

        // Base movie search query (FTS match + core validity filters)
        StringBuilder sql = new StringBuilder("""
        SELECT m.*
        FROM movie m
        JOIN movie_fts fts ON fts.rowid = m.id
        WHERE fts.movie_name MATCH ?
          AND m.is_active = 1
          AND m.year > 0
          AND m.runtime_minutes > 0
    """);

    // Parameters bound to the prepared SQL query
    List<Object> params = new ArrayList<>();
    params.add(likePattern);
    // Content-type filters (movies / series / shorts)
    boolean movieChecked  = Boolean.TRUE.equals(movie);    // is_series = 0 AND is_shorts = 0
    boolean seriesChecked = Boolean.TRUE.equals(series);   // is_series = 1
    boolean shortsChecked = Boolean.TRUE.equals(shorts);   // is_shorts = 1

    List<String> typeClauses = new ArrayList<>();
    // If user checked "movies"
    if (movieChecked) {
        typeClauses.add("(m.is_series = ? AND m.is_shorts = ?)");
        params.add(0); // is_series
        params.add(0); // is_shorts
    }
    // If user checked "series"
    if (seriesChecked) {
        typeClauses.add("(m.is_series = ?)");
        params.add(1); // is_series = 1
    }
    // If user checked "Shorts"
    if (shortsChecked) {
        typeClauses.add("(m.is_shorts = ?)");
        params.add(1); // is_shorts = 1
    }

    // If at least one category was selected, apply OR-grouped filter
    if (!typeClauses.isEmpty()) {
        sql.append(" AND (")
        .append(String.join(" OR ", typeClauses))
        .append(") ");
    }
    
    // Rating filter
    if (Boolean.TRUE.equals(rated)) {
        sql.append(" AND m.rating IS NOT NULL AND m.rating != '' ");
    }
    sql.append(" ORDER BY ").append(sortColumn)
       .append(" LIMIT ? OFFSET ?");

    params.add(limit);
    params.add(offset);

        // Bind the same FTS keyword to both columns
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    // Returns total result count for pagination
    public int countMovies(String keyword){
        // Exact tconst lookup bypasses FTS
        if (keyword != null && keyword.matches("tt\\d{7,8}")) {
            Movie m = findByTconst(keyword);
            return (m != null) ? 1 : 0;
        }
        // Prepare safe FTS query (prefix search)
        String safe = sanitizeFTS(keyword);
        String ftsPattern = safe + "*"; // same prefix search
        if (safe.isBlank()) {
            return 0;
        }
        String sql = """
        SELECT COUNT(*)
        FROM movie m
        JOIN movie_fts fts ON fts.rowid = m.id
        WHERE fts.movie_name MATCH ?
          AND m.is_active = 1
          AND m.year > 0
          AND m.runtime_minutes > 0
    """;
        return jdbcTemplate.queryForObject(sql, Integer.class, ftsPattern);
    }

    //upsert function (inserts new data into the data base, so it does not replace rows but updates them)
    public int upsertFromImdbFile(File tsvGz, boolean onlyMovies, boolean markInactiveMissing) throws IOException {
    jdbcTemplate.execute("PRAGMA foreign_keys = ON");
    jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_movie_tconst ON movie(tconst)");

    //define sql upsertion string, with the inputs from imdb basic movie dataset
    final String upsertSql =
        "INSERT INTO movie (tconst, movie_name, original_movie_name, year, runtime_minutes, is_active, is_series, is_shorts) " +
        "VALUES (?, ?, ?, ?, ?, 1, ?, ?) " +
        "ON CONFLICT(tconst) DO UPDATE SET " +
        "  movie_name=excluded.movie_name, " +
        "  original_movie_name=excluded.original_movie_name, " +
        "  year=excluded.year, " +
        "  runtime_minutes=excluded.runtime_minutes, " +
        "  is_active=1, " +
        "  is_series=excluded.is_series, "+
        "  is_shorts=excluded.is_shorts";

    //if any movies has been removed from imdb's basic data, then remove it from the database
    if (markInactiveMissing) {
        jdbcTemplate.execute("CREATE TEMP TABLE IF NOT EXISTS tmp_seen(tconst TEXT PRIMARY KEY) WITHOUT ROWID");
        jdbcTemplate.update("DELETE FROM tmp_seen");
    }

    int processed = 0;
    //get current year
    int currentYear = Year.now(ZoneId.systemDefault()).getValue();

    //insertion function of basic movie data within the movie sql table
    try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(tsvGz));

        //define a buffer for the insertions
         BufferedReader br = new BufferedReader(new InputStreamReader(gzis, StandardCharsets.UTF_8))) {

        //check if movie data is null, if null return
        String header = br.readLine();
        if (header == null) return 0;

        //set upsertion buffer to a 1000 upsertions
        ArrayList<Object[]> upserts = new ArrayList<>(1000);
        ArrayList<Object[]> seen    = new ArrayList<>(1000);
        String line;

        //as long as there a data run this while function
        while ((line = br.readLine()) != null) {
            //define a list which contains the movie data, split up in less then 9 data sets
            String[] c = line.split("\t", -1);
            if (c.length < 9) continue;

            //define each dataset tconst, titletype...
            String tconst = c[0];
            String titleType = c[1];

            //if movie type is movie, tvseries or short continue
            if (onlyMovies && !( "movie".equals(titleType) || "tvSeries".equals(titleType) || "short".equals(titleType) )) {continue;}

            //if title is series or short set seriesType or ShortsType to 1
            Integer seriesType;
            Integer shortsType;
            if ("tvSeries".equals(titleType)){seriesType = 1;}else{seriesType = 0;}
            if ("short".equals(titleType)){shortsType = 1;}else{shortsType = 0;}

            //define the rest of the movie data
            String primaryTitle  = normalize(c[2]);
            String originalTitle = normalize(c[3]);
            String movieName = (primaryTitle != null) ? primaryTitle
                             : (originalTitle != null) ? originalTitle
                             : tconst;

            Integer startYear = parseIntOrNull(c[5]);
            Integer runtime   = parseIntOrNull(c[7]);

            //if startyear is less then 1888 (first movie got made there) and more then current year stop
            if (startYear != null && (startYear < 1888 || startYear > currentYear)) continue;

            //if runtime is less then 0 then stop
            if (runtime == null || runtime <= 0) continue;

            //add this tilte to the upsertion
            upserts.add(new Object[]{ tconst, movieName, originalTitle, startYear, runtime, seriesType, shortsType });
            if (markInactiveMissing) seen.add(new Object[]{ tconst });
            processed++;

            //when there is a 1000 or more upsertions, upsert it into the db
            if (upserts.size() >= 1000) {
                jdbcTemplate.batchUpdate(upsertSql, upserts);
                upserts.clear();
                if (markInactiveMissing) {
                    jdbcTemplate.batchUpdate("INSERT OR IGNORE INTO tmp_seen(tconst) VALUES (?)", seen);
                    seen.clear();
                }
            }
        }

        //if upsertions are not empty insert the rest of the upsertions
        if (!upserts.isEmpty()) {
            jdbcTemplate.batchUpdate(upsertSql, upserts);
            if (markInactiveMissing) {
                jdbcTemplate.batchUpdate("INSERT OR IGNORE INTO tmp_seen(tconst) VALUES (?)", seen);
            }
        }
    }

    //delete all titles which has been dropped by imdb
    if (markInactiveMissing) {
        jdbcTemplate.update("UPDATE movie SET is_active = 0 WHERE tconst NOT IN (SELECT tconst FROM tmp_seen)");
        jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_seen");
    }
    return processed;
    }

    //function to normalize text strings
    private static String normalize(String s) {
        return (s == null || s.isEmpty() || "\\N".equals(s)) ? null : s;
    }
    //function to define if there is an integer if not return null
    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty() || "\\N".equals(s)) return null;
        try { return Integer.valueOf(s); } catch (NumberFormatException e) { return null; }
    }

    //Function to merge temporary ratings table with movie table
    public void mergeRatingsFromImdbFile(File ratingsTsvGz) throws IOException {
        jdbcTemplate.execute("PRAGMA foreign_keys = ON");

        // create temporary ratings table, with rating datadump information
        jdbcTemplate.execute("""
            CREATE TEMP TABLE IF NOT EXISTS tmp_ratings(
                tconst TEXT PRIMARY KEY,
                rating TEXT,
                votes  INTEGER
            ) WITHOUT ROWID
        """);
        jdbcTemplate.update("DELETE FROM tmp_ratings");

        //Try to read through the data dump file and insert it into the temp table
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(ratingsTsvGz));
            BufferedReader br = new BufferedReader(new InputStreamReader(gzis, StandardCharsets.UTF_8))) {

            //define variable that is equal to the 3 datasets in the dump, if null return
            String header = br.readLine(); // tconst\taverageRating\tnumVotes
            if (header == null) return;

            //Buffer to only inser 10.000 insertions at a time
            List<Object[]> batch = new ArrayList<>(10_000);

            //insertion function for the datasets per 10.000 dataset
            String line;
            while ((line = br.readLine()) != null) {
                //split values up so each dataset have 3 values
                String[] c = line.split("\t", -1);
                if (c.length < 3) continue;

                //define the 3 values from the table
                String tconst = c[0];
                String rating = c[1];     // keep as TEXT per your schema
                Integer votes  = parseIntOrNull(c[2]);

                //add the 3 values to the batch of 10.000 insertions
                batch.add(new Object[]{ tconst, rating, votes });

                //insert batch when hitting or over 10.000
                if (batch.size() >= 10_000) {
                    jdbcTemplate.batchUpdate("INSERT OR REPLACE INTO tmp_ratings(tconst, rating, votes) VALUES (?,?,?)", batch);
                    batch.clear();
                }
            }
            //if batch is not empty after insertions, insert missing values
            if (!batch.isEmpty()) {
                jdbcTemplate.batchUpdate("INSERT OR REPLACE INTO tmp_ratings(tconst, rating, votes) VALUES (?,?,?)", batch);
            }
        }

        // update movie table with ratings from tmp_rating table using tconst
        jdbcTemplate.update("""
            UPDATE movie
            SET rating = (SELECT rating FROM tmp_ratings r WHERE r.tconst = movie.tconst)
            WHERE EXISTS (SELECT 1 FROM tmp_ratings r WHERE r.tconst = movie.tconst)
        """);

        jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_ratings");
    }



}
