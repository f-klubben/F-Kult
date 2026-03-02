package dk.fklub.fkult;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.File;

@SpringBootApplication
public class FkultApplication {

    public static void main(String[] args) {
        SpringApplication.run(FkultApplication.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void ensureDatabaseFolderExists() {
        File folder = new File("database");
        if (!folder.exists() && folder.mkdirs()) {
            System.out.println("✔ Created database directory: " + folder.getAbsolutePath());
        }
    }
}
