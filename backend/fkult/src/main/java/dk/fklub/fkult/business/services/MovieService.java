package dk.fklub.fkult.business.services;

import dk.fklub.fkult.persistence.entities.Movie;
import dk.fklub.fkult.persistence.repository.MovieRepository;
import dk.fklub.fkult.presentation.DTOs.MovieRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovieService {
    private final String IMDB_URL = "https://www.imdb.com/title/";
    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Search movies via repository and map results to MovieRequest
    public List<MovieRequest> searchMovies(String query, int page, int limit, String sortBy, String direction, Boolean movie, Boolean series, Boolean shorts, Boolean rated) {
        //partial matches (case-insensitive)
        List<MovieRequest> movieRequests = new ArrayList<MovieRequest>();
        movieRepository.searchMovies(query, page, limit, sortBy, direction, movie, series, shorts, rated).forEach(m -> {
            MovieRequest movieRequest = new MovieRequest(
                    m.getTconst(),
                    m.getMovieName(),
                    m.getRuntimeMinutes(),
                    m.getYear(),
                    m.getRating(),
                    m.getPosterURL(),
                    m.getIsSeries(),
                    m.getIsShorts()
                    );
            movieRequests.add(movieRequest);
        });
        return movieRequests;
    }

    // Count results for pagination 
    public int getMovieSearchCount(String query){
        int count = movieRepository.countMovies(query);
        return count;
    }
    //You give it a list of movieIds it gives you a list of DTO MovieRequests with all the movie data
    public List<MovieRequest> getMoviesByIds(List<Long> movieIds) {
        List<MovieRequest> movieRequests = new ArrayList<>();

        for (Long movieId : movieIds) {
            Movie movie = movieRepository.findById(movieId);
            if (movie == null) {
                throw new RuntimeException("Movie not found with id: " + movieId);
            }
            String posterURL = getPosterURL(movie);
            MovieRequest movieRequest = new MovieRequest(
                    movie.getId(),
                    movie.getMovieName(),
                    posterURL,
                    movie.getRating(),
                    movie.getRuntimeMinutes(),
                    movie.getYear()
            );
            movieRequests.add(movieRequest);
        }
        return movieRequests;
    }
    //You give it a list of Tconsts it gives you a list of DTO MovieRequests with all the movie data
    public List<MovieRequest> getMoviesByTconsts(List<String> tConsts){
        List<MovieRequest> movieRequests = new ArrayList<>();

        for (String tConst : tConsts) {
            Movie movie = movieRepository.findByTconst(tConst);
            if (movie == null) {
                throw new RuntimeException("Movie not found with tconst: " + tConst);
            }
            String posterURL = getPosterURL(movie);
            MovieRequest movieRequest = new MovieRequest(
                    movie.getId(),
                    movie.getMovieName(),
                    posterURL,
                    movie.getRating(),
                    movie.getRuntimeMinutes(),
                    movie.getYear()
            );
            movieRequests.add(movieRequest);
        }
        return movieRequests;
    }

    public String getPosterByTconst(String tConst){
        Movie movie = movieRepository.findByTconst(tConst);
        return getPosterURL(movie);
    }

    public String getPosterById(long id){
        Movie movie = movieRepository.findById(id);
        return getPosterURL(movie);
    }

    public String getPosterURL(Movie movie) {
        if (movie.getTconst() == null) {//if no tConst is given we can't find the movie so return null
            System.out.println("Movie " + movie.getId() + " has no tconst");
            return null;
        }
        if(movie.getPosterURL() != null && !movie.getPosterURL().isBlank()){
            return movie.getPosterURL(); //if posterURL already cached, just return cached version
        }

        String tConst = movie.getTconst() + "/";
        String movieURL = IMDB_URL + tConst;

        try {
            // Use browser-like headers to avoid IMDb giving a 403 forbidden
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/119.0.0.0 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            //Fires the actual network call to IMDb
            ResponseEntity<String> response = exchangeImdb(movieURL, entity);


            String html = response.getBody();
            if (html == null || html.isEmpty()) {
                System.out.println("Failed to fetch HTML for movie " + movie.getId());
                return null;
            }
            //Jsoup library helps parse the html document
            Document doc = Jsoup.parse(html);
            Element ogImage = doc.selectFirst("meta[property=og:image]"); //this is the poster HTML element
            if (ogImage != null) {
                String poster = ogImage.attr("content");//Element.attr saves HTML element's attribute to a string
                movieRepository.updatePosterURL(movie.getId(), poster);//Cache poster in DB so we don't have to ask IMDb next time
                movie.setPosterURL(poster);
                System.out.println("Found poster URL: " + poster +" caching URL...");
                return poster;
            } else {
                System.out.println("No og:image meta tag found for " + movieURL);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // for testing purposes
    public ResponseEntity<String> exchangeImdb(String url, HttpEntity<String> entity) {
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }

    public String getTconstById(long id){
        Movie movie = movieRepository.findById(id);
        return movie.getTconst();
    }
}