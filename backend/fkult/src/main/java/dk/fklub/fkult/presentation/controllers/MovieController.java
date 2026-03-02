package dk.fklub.fkult.presentation.controllers;

import dk.fklub.fkult.business.services.MovieService;

import dk.fklub.fkult.presentation.DTOs.MovieRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping("/batchById")//user sends a bunch of movie ids, we send them a bunch of movies
    public ResponseEntity<?> getMovieIds(@RequestBody List<Long> movieIds) {
        try {
            List<MovieRequest> movies = movieService.getMoviesByIds(movieIds);
            return ResponseEntity.ok(movies);
        } catch (RuntimeException e) {
            // Return 404 with a JSON error message
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        }
    }

    @PostMapping("/batchByTconst")//user sends a bunch of tConsts, we send them a bunch of movies
        public ResponseEntity<?> getMoviesByTconst(@RequestBody List<String> tconsts) {
            try {
                List<MovieRequest> movies = movieService.getMoviesByTconsts(tconsts);
                return ResponseEntity.ok(movies);
            } catch (RuntimeException e) {
                // Return 404 with a JSON error message
                Map<String, String> errorBody = new HashMap<>();
                errorBody.put("error", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
            }
        }


    //Search by movie name
    @GetMapping("/search")
    public ResponseEntity<?> searchMovies(@RequestParam String q,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "6") int limit,
                                                           @RequestParam(defaultValue = "rating") String sortBy, 
                                                           @RequestParam(defaultValue = "desc") String direction,
                                                           @RequestParam(required = false) Boolean movie,
                                                           @RequestParam(required = false) Boolean series,
                                                           @RequestParam(required = false) Boolean shorts,
                                                           @RequestParam(required = false) Boolean rated ){
        //if parameters aren't defined by the user, we define some defaults.
        if (limit > 40) limit = 40;//we don't allow asking for more than 40 movies
        if (limit < 0) limit = 0;// we don't allow asking for less than 0 movies
        List<MovieRequest> results = movieService.searchMovies(q, page, limit, sortBy, direction, movie, series, shorts, rated);

        if (results.isEmpty()) {
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("error", "No movies found for query: " + q);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
        }

        return ResponseEntity.ok(results);
    } // to test the search: GET http://localhost:8080/api/movies/search?q=MovieTitle&page=1

    @GetMapping("/search/count")//counts how many movies a search query will result
    public ResponseEntity<Integer> countMovies(@RequestParam String q){
        Integer count = movieService.getMovieSearchCount(q);
        return ResponseEntity.ok(count);
    }


    @GetMapping("/poster/{tconst}")//gives you a posterURL given a tconst
    public ResponseEntity<String> getPoster(@PathVariable String tconst){
        String posterURL = movieService.getPosterByTconst(tconst);
        return ResponseEntity.ok(posterURL);
    }

    //IMDb preview (scraping http to json)
    @GetMapping("/preview/{tconst}")
    public ResponseEntity<Map<String, String>> getPreview(@PathVariable String tconst) {
        Map<String, String> preview = new HashMap<>();
        try {
            String url = "https://www.imdb.com/title/" + tconst + "/";
            Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();

            preview.put("title", getMeta(doc, "og:title"));
            preview.put("description", getMeta(doc, "og:description"));
            preview.put("image", getMeta(doc, "og:image"));
            preview.put("url", getMeta(doc, "og:url"));

            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            preview.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(preview);
        }
    }

    @GetMapping("/poster/id/{id}")
    public String tconstById(@PathVariable long id) {
        return movieService.getPosterById(id);
    }

    private String getMeta(Document doc, String property) {
        Element meta = doc.selectFirst("meta[property=" + property + "]");
        return meta != null ? meta.attr("content") : "";
    }

}