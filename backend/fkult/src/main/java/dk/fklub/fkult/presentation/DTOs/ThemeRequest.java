package dk.fklub.fkult.presentation.DTOs;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ThemeRequest {
    private Long themeId;
    private String name;
    private Long userId;
    private List<Long> movieIds;
    private List<String> drinkingRules;
    private List<String> tConsts;
    private String username;
    private LocalDateTime timestamp;
    //Springboot requires a default constructor to deserialize using @RequestBody on endpoints
    public ThemeRequest(){}

    public ThemeRequest(Long themeId, String name, Long userId, List<Long> movieIds, List<String> drinkingRules){
        this.themeId = themeId;
        this.name = name;
        this.userId = userId;
        this.movieIds = movieIds;
        this.drinkingRules = drinkingRules;
    }
    public ThemeRequest(List<String> tConsts, Long themeId, String name, String username, List<String> drinkingRules){
        this.themeId = themeId;
        this.name = name;
        this.username = username;
        this.tConsts = tConsts;
        this.drinkingRules = drinkingRules;
    }

    public ThemeRequest(String name, String username, List<String> tConsts, List<String> drinkingRules){
        this.name = name;
        this.username = username;
        this.tConsts = tConsts;
        this.drinkingRules = drinkingRules;
    }
    public ThemeRequest(Long themeId, String name, Long userId, List<Long> movieIds, List<String> drinkingRules, LocalDateTime timestamp) {
        this.themeId = themeId;
        this.name = name;
        this.userId = userId;
        this.movieIds = movieIds;
        this.drinkingRules = drinkingRules;
        this.timestamp = timestamp;
    }
    public ThemeRequest(Long themeId, String name, String username, Long userId, List<Long> movieIds, List<String> drinkingRules, LocalDateTime timestamp) {
        this.themeId = themeId;
        this.name = name;
        this.username = username;
        this.userId = userId;
        this.movieIds = movieIds;
        this.drinkingRules = drinkingRules;
        this.timestamp = timestamp;
    }

    //getters
    public Long getThemeId(){
        return themeId;
    }
    public String getName() { return name; }
    public Long getUserId() { return userId; }
    public List<Long> getMovieIds() { return movieIds; }
    public List<String> getDrinkingRules() { return drinkingRules; }
    public List<String> gettConsts() { return tConsts; }
    public String getUsername(){ return username; }
    public LocalDateTime getTimestamp() { return timestamp; }

    //setters
    public void setName(String name) { this.name = name; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setMovieIds(List<Long> movieIds) { this.movieIds = movieIds; }
    public void setDrinkingRules(List<String> rules) { this.drinkingRules = rules; }
    public void settConsts(List<String> tConsts) {this.tConsts = tConsts;}
    public void setUsername(String username){this.username = username;}
    public void setTimestamp(LocalDateTime timestamp){this.timestamp = timestamp;}
    public void setThemeId(Long themeId) {this.themeId = themeId;}

    @Override
    public String toString() {
        return "ThemeRequest{" +
                "themeId=" + themeId +
                ", name='" + name + '\'' +
                ", userId=" + userId +
                ", username='" + username + '\'' +
                ", movieIds=" + (movieIds != null ? movieIds.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ")) : "null") +
                ", drinkingRules=" + (drinkingRules != null ? String.join(", ", drinkingRules) : "null") +
                ", tConsts=" + (tConsts != null ? String.join(", ", tConsts) : "null") +
                ", timestamp=" + timestamp +
                '}';
    }

}
