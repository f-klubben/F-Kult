package dk.fklub.fkult.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component //spring Bean managed by application context
@Order(1)//makes sure this is the first thing that runs after startup
//it creates SQL triggers to sync the movie_fts table with the movie table in the database
public class TriggerInitializer implements ApplicationRunner {//application runner allows us to run code after springboot has started.
    //datasource is just the database
    private final DataSource dataSource;

    public TriggerInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {//application args must be there because applicationrunner requires it

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Avoid SQLITE_BUSY by giving SQLite time to lock/unlock
            stmt.execute("PRAGMA busy_timeout = 4000;");

            // Create triggers
            //triggers simply cause a reaction when something else happens.
            //in this case when we insert a movie into the normal movie table, we also insert it into the fts table
            stmt.execute("""
                CREATE TRIGGER IF NOT EXISTS movie_ai
                AFTER INSERT ON movie
                BEGIN
                    INSERT INTO movie_fts(movie_name, original_movie_name)
                    VALUES (NEW.movie_name, NEW.original_movie_name);
                END;
            """);
            //if we delete a movie from the movie table, we also delete it from the fts table
            stmt.execute("""
                CREATE TRIGGER IF NOT EXISTS movie_ad
                AFTER DELETE ON movie
                BEGIN
                    DELETE FROM movie_fts WHERE rowid = OLD.id;
                END;
            """);
            //if we update a movie in the movie table we also update in the fts table
            stmt.execute("""
                CREATE TRIGGER IF NOT EXISTS movie_au
                AFTER UPDATE ON movie
                BEGIN
                    UPDATE movie_fts
                    SET movie_name = NEW.movie_name,
                        original_movie_name = NEW.original_movie_name
                    WHERE rowid = OLD.id;
                END;
            """);
            //the OLD and NEW keywords refer to the OLD row being updated or delete, or the NEW row being inserted, or updated
            System.out.println("SQLite triggers created successfully.");
        }
    }
}

