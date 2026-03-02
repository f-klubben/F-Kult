package dk.fklub.fkult.config;

import dk.fklub.fkult.business.services.ImdbMovieImportService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class ImportSchedular {

    private final ImdbMovieImportService svc;

    public ImportSchedular(ImdbMovieImportService svc) {
        this.svc = svc;
    }

    //"0 0 4 ? * MON", zone = "Europe/Copenhagen" (every monday at 4)
    // Runs the download script every Monday at 04:00 am
    @Scheduled(cron = "0 0 4 ? * MON", zone = "Europe/Copenhagen")
    public void weeklyImdbUpdate() {
        try {
            int n = svc.weeklyRefresh();
            System.out.println("IMDb weekly refresh complete. rows_processed = " + n);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}


