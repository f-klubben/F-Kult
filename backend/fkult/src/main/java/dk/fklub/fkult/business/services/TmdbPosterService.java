
package dk.fklub.fkult.business.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import org.springframework.context.annotation.Profile;

@Service
@Profile("!setup")
public class TmdbPosterService {

    // TMDb API endpoints
    private static final String TMDB_FIND_URL =
            "https://api.themoviedb.org/3/find/{tconst}?external_source=imdb_id";

    private static final String TMDB_IMAGE_URL =
            "https://image.tmdb.org/t/p/w500";

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiToken;

    // Read API token from application.properties
    public TmdbPosterService(
            @Value("${tmdb.api.read-token}") String apiToken) {
        this.apiToken = apiToken;
    }

    // Find a movie or series poster using its IMDb ID
    public String getPosterURL(String tconst, boolean isSeries) {

        // Validate IMDb ID
        if (tconst == null || !tconst.matches("tt\\d{7,}")) {
            return null;
        }

        // Configure HTTP headers with API token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Request movie information from TMDb
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                TMDB_FIND_URL,
                HttpMethod.GET,
                entity,
                JsonNode.class,
                tconst
        );

        JsonNode body = response.getBody();

        if (body == null) {
            return null;
        }

        // TMDb separates movies and TV series
        String resultType = isSeries ? "tv_results" : "movie_results";
        JsonNode results = body.path(resultType);

        // Return null if no matching movie/series exists
        if (!results.isArray() || results.isEmpty()) {
            return null;
        }

        // Extract poster path from the first result
        JsonNode posterNode = results.get(0).path("poster_path");

        if (!posterNode.isTextual() || posterNode.asText().isBlank()) {
            return null;
        }

        // Construct and return the complete poster URL
        return TMDB_IMAGE_URL + posterNode.asText();
    }
}
