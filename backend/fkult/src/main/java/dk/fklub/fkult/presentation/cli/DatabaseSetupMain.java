package dk.fklub.fkult.presentation.cli;

import dk.fklub.fkult.FkultApplication;
import dk.fklub.fkult.business.services.ImdbMovieImportService;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class DatabaseSetupMain {

    // Controls the spinner thread while the import is running
    private static volatile boolean loading = true;

    public static void main(String[] args) {

        // Start Spring Boot in CLI mode (no web server, reduced logging)
        ConfigurableApplicationContext context =
                new SpringApplicationBuilder(FkultApplication.class)
                        .profiles("setup")
                        .web(WebApplicationType.NONE)
                        .bannerMode(org.springframework.boot.Banner.Mode.OFF)
                        .logStartupInfo(false)
                        .run(args);

        //spinner animation
        Thread spinner = new Thread(() -> {
            String[] frames = {"|", "/", "-", "\\"};
            int i = 0;
            while (loading) {
                System.out.print("\rImporting IMDb data... " + frames[i++ % frames.length]);
                System.out.flush();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        spinner.start();

        try {
            // Obtain the IMDb import service from the Spring context
            ImdbMovieImportService svc = context.getBean(ImdbMovieImportService.class);

            // Run the dataset download + database import
            int n = svc.weeklyRefresh();

            // Stop spinner
            loading = false;
            spinner.join();

            // Clear spinner line and print final result
            System.out.print("\r                                                           \r");
            System.out.println("[Setup] Import complete. rows_processed = " + n);

        } catch (Exception e) {

            // Ensure spinner stops even if an error occurs
            loading = false;
            try {
                spinner.join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            System.out.print("\r                                                           \r");
            e.printStackTrace();
            System.exit(1);
        } finally {
            context.close();
        }
    }
}