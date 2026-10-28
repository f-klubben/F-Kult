
package dk.fklub.fkult.config;

import dk.fklub.fkult.business.services.ImdbMovieImportService;
import dk.fklub.fkult.business.services.TmdbPosterImportService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

@Profile("!setup")
@EnableScheduling
@Component
@Order(3)
public class ImportSchedular implements ApplicationRunner {

    private static final Logger logger =
            LoggerFactory.getLogger(ImportSchedular.class);

    private final ImdbMovieImportService imdbImportService;
    private final TmdbPosterImportService posterImportService;

    // Prevent IMDb and TMDb imports from accessing
    // the database simultaneously
    private final ReentrantLock importLock = new ReentrantLock();

    // Only start poster import after initial IMDb bootstrap
    private volatile boolean startupComplete = false;

    public ImportSchedular(
            ImdbMovieImportService imdbImportService,
            TmdbPosterImportService posterImportService) {

        this.imdbImportService = imdbImportService;
        this.posterImportService = posterImportService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Runs after ImdbChecker (@Order(2))
        startupComplete = true;
        logger.info("Import scheduler initialized.");
    }

    // Update IMDb dataset every Monday at 04:00
    @Scheduled(cron = "0 0 4 ? * MON", zone = "Europe/Copenhagen")
    public void weeklyImdbUpdate() {

        if (!startupComplete) {
            return;
        }

        importLock.lock();

        try {
            logger.info("Starting weekly IMDb update...");

            int processed = imdbImportService.weeklyRefresh();

            logger.info(
                    "IMDb weekly refresh complete. Rows processed: {}",
                    processed
            );

        } catch (Exception e) {
            logger.error("IMDb weekly refresh failed.", e);

        } finally {
            importLock.unlock();
        }
    }

    // Continuously process pending posters in batches
    @Scheduled(
        fixedDelayString = "${tmdb.import.scheduler-delay-ms:60000}",
        initialDelayString = "${tmdb.import.initial-delay-ms:60000}"
    )
    public void scheduledPosterImport() {

        // Wait until initial IMDb setup has finished
        if (!startupComplete) {
            return;
        }

        // Skip this run if another database import is active
        if (!importLock.tryLock()) {
            logger.info("Another import is running. Poster import postponed.");
            return;
        }

        try {
            int processed = posterImportService.importPendingPosters();

            if (processed > 0) {
                logger.info(
                        "TMDb poster batch complete. Processed: {}",
                        processed
                );
            }

        } catch (Exception e) {
            logger.error("TMDb poster import failed.", e);

        } finally {
            importLock.unlock();
        }
    }
}



