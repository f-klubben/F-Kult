
package dk.fklub.fkult.business.services;

import dk.fklub.fkult.persistence.entities.Movie;
import dk.fklub.fkult.persistence.repository.MovieRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Profile("!setup")
public class TmdbPosterImportService {

    private static final Logger logger =
            LoggerFactory.getLogger(TmdbPosterImportService.class);

    private final MovieRepository movieRepository;
    private final TmdbPosterService tmdbPosterService;

    private final int batchSize;
    private final int maxMoviesPerRun;
    private final long requestDelayMs;

    // Prevent multiple imports from running simultaneously
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TmdbPosterImportService(
            MovieRepository movieRepository,
            TmdbPosterService tmdbPosterService,
            @Value("${tmdb.import.batch-size:100}") int batchSize,
            @Value("${tmdb.import.max-movies-per-run:1000}") int maxMoviesPerRun,
            @Value("${tmdb.import.request-delay-ms:500}") long requestDelayMs) {

        this.movieRepository = movieRepository;
        this.tmdbPosterService = tmdbPosterService;

        if (batchSize <= 0 || maxMoviesPerRun <= 0 || requestDelayMs < 0) {
            throw new IllegalArgumentException("Invalid TMDb import configuration");
        }

        this.batchSize = batchSize;
        this.maxMoviesPerRun = maxMoviesPerRun;
        this.requestDelayMs = requestDelayMs;
    }

    // Import pending posters from the database in controlled batches
    public int importPendingPosters() {

        // Prevent overlapping import jobs
        if (!running.compareAndSet(false, true)) {
            logger.warn("TMDb poster import is already running.");
            return 0;
        }

        int processed = 0;
        int found = 0;
        int notFound = 0;

        logger.info("Starting TMDb poster import...");

        try {
            while (processed < maxMoviesPerRun) {

                // Fetch next batch of pending movies
                int limit = Math.min(batchSize, maxMoviesPerRun - processed);

                List<Movie> movies =
                        movieRepository.findMoviesMissingPosters(limit);

                // No more pending movies
                if (movies.isEmpty()) {
                    logger.info("No more pending movie posters.");
                    break;
                }

                for (Movie movie : movies) {

                    if (Thread.currentThread().isInterrupted()) {
                        return processed;
                    }

                    try {
                        // Fetch poster from TMDb using IMDb ID
                        String posterURL = tmdbPosterService.getPosterURL(
                                movie.getTconst(),
                                movie.getIsSeries()
                        );

                        if (posterURL != null && !posterURL.isBlank()) {

                            // Poster found: save URL and status
                            movieRepository.updatePosterURL(
                                    movie.getId(),
                                    posterURL
                            );

                            found++;

                        } else {

                            // TMDb has no matching poster
                            movieRepository.markPosterNotFound(
                                    movie.getId()
                            );

                            notFound++;
                        }

                    } catch (HttpClientErrorException.TooManyRequests e) {

                        // Respect TMDb rate limiting
                        logger.warn(
                                "TMDb rate limit reached. Stopping import."
                        );

                        return processed;

                    } catch (RestClientException e) {

                        // Network or API failure - keep movie PENDING
                        logger.error(
                                "TMDb request failed for {}. Stopping import.",
                                movie.getTconst(), e
                        );

                        return processed;

                    } catch (RuntimeException e) {

                        // Unexpected error - avoid an infinite retry loop
                        logger.error(
                                "Poster import failed for {}. Stopping import.",
                                movie.getTconst(), e
                        );

                        return processed;
                    }

                    processed++;

                    // Log progress every 100 movies
                    if (processed % 100 == 0) {
                        logger.info(
                                "Poster import progress: {} processed, {} found, {} missing",
                                processed, found, notFound
                        );
                    }

                    // Delay between requests
                    try {
                        Thread.sleep(requestDelayMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return processed;
                    }
                }
            }

            return processed;

        } finally {

            logger.info(
                    "TMDb import stopped. Processed: {}, Found: {}, Missing: {}",
                    processed, found, notFound
            );

            running.set(false);
        }
    }
}
