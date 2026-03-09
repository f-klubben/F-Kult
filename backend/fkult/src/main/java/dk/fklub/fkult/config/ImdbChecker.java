package dk.fklub.fkult.config;
import dk.fklub.fkult.business.services.ImdbMovieImportService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Profile;


//component which runs secondly on upstart of the program
@Profile("!setup")
@Component
@Order(2)// ensure this runs early
public class ImdbChecker implements ApplicationRunner {

    private final ImdbMovieImportService importService;
    private final Path localPath;
    private volatile boolean loading = false;

     //checker function, that checks if "title.basics.tsv.gz" exists by specific path
    public ImdbChecker(ImdbMovieImportService importService, @Value("${imdb.local-path:database/title.basics.tsv.gz}") String localPathStr) {
        this.importService = importService;
        this.localPath = Paths.get(localPathStr);
    }

    //function that checks if dataset file exists, if it does return message, if not call weeklyRefresh function
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (Files.exists(localPath)) {
            System.out.println("[IMDb bootstrap] Dataset already present: " + localPath.toAbsolutePath());
            return;
        }

        System.out.println("[IMDb bootstrap] Dataset missing; downloading & importing...");

        loading = true;
        Thread spinner = createSpinner("[IMDb bootstrap] Importing IMDb data... ");

        spinner.start();

        try {
            int n = importService.weeklyRefresh();

            loading = false;
            spinner.join();

            System.out.print("\r                                                                                \r");
            System.out.println("[IMDb bootstrap] Import complete. rows_processed=" + n);
        } catch (Exception e) {
            loading = false;
            spinner.join();

            System.out.print("\r                                                                                \r");
            System.out.println("[IMDb bootstrap] Import failed.");
            throw e;
        }
    }

    //spinning animation
    private Thread createSpinner(String message) {
        return new Thread(() -> {
            String[] frames = {"|", "/", "-", "\\"};
            int i = 0;

            while (loading) {
                System.out.print("\r" + message + frames[i++ % frames.length]);
                System.out.flush();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
    }
}