 package dk.fklub.fkult.business.services;

import dk.fklub.fkult.persistence.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;


@Service
public class ImdbMovieImportService {

    //api call links to imdb bulk datasets
    private static final String BASICS_URL  = "https://datasets.imdbws.com/title.basics.tsv.gz";
    private static final String RATINGS_URL = "https://datasets.imdbws.com/title.ratings.tsv.gz";

    //folder pathing and file defining
    private static final Path DATA_DIR      = Paths.get("database");
    private static final Path BASICS_LOCAL  = DATA_DIR.resolve("title.basics.tsv.gz");
    private static final Path RATINGS_LOCAL = DATA_DIR.resolve("title.ratings.tsv.gz");

    private final MovieRepository movieRepository;

    //connection to movie repository
    public ImdbMovieImportService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    //Function that runs the weekly, downloads two data dumps one with basic movie info and one with movie ratings 
    @Transactional
    public int weeklyRefresh() throws IOException {
        System.out.println("DB path: " + Paths.get("database/F-Kult.DB").toAbsolutePath());

        // Basic movie data
        Path basicsTmp = downloadTemp(BASICS_URL, "title.basics-");
        replaceOldWithTemp(basicsTmp, BASICS_LOCAL);
        int upserted = movieRepository.upsertFromImdbFile(BASICS_LOCAL.toFile(), true, true);

        // ratings
        Path ratingsTmp = downloadTemp(RATINGS_URL, "title.ratings-");
        replaceOldWithTemp(ratingsTmp, RATINGS_LOCAL);

        //merge the two data dumps
        movieRepository.mergeRatingsFromImdbFile(RATINGS_LOCAL.toFile());

        //returns the number of inserts/updates that are inserted
        return upserted;
    }
    //Download remote file into a temporary local file
    protected Path downloadTemp(String url, String prefix) throws IOException {

        //Ensure the data directory exists
        Files.createDirectories(DATA_DIR);

        //Create a temporary file inside DATA_DIR with the given prefix
        Path tmp = Files.createTempFile(DATA_DIR, prefix, ".tsv.gz.tmp");

        // Download the file from the URL and write it to the temporary file
        try (InputStream in = java.net.URI.create(url).toURL().openStream()) {
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.deleteIfExists(tmp); // If download fails, delete the partial temp file
            throw e;
        }
        return tmp;
    }

    //if files already exists, replace them
    protected void replaceOldWithTemp(Path tmp, Path dest) throws IOException {
        Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
