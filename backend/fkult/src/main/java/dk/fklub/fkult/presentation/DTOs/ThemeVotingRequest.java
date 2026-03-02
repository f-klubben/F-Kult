package dk.fklub.fkult.presentation.DTOs;

import java.util.List;

public class ThemeVotingRequest {

    private Long themeId;
    private Long votes;
    private String themeName;
    private String submitterName;
    private List<String> movieNames;
    private List<String> moviePosters;
    private List<String> drinkingRules;
    private List<Long> movieIds;
    private List<Double> ratings;
    private List<Long> runTimes;

    public ThemeVotingRequest() {}

    public ThemeVotingRequest(Long themeId, Long votes, String themeName, String submitterName, List<String> movieNames, List<String> moviePosters, List<String> drinkingRules, List<Long> movieIds, List<Double> ratings, List<Long> runTimes) {
        this.themeId = themeId;
        this.votes = votes;
        this.themeName = themeName;
        this.submitterName = submitterName;
        this.movieNames = movieNames;
        this.moviePosters = moviePosters;
        this.drinkingRules = drinkingRules;
        this.movieIds = movieIds;
        this.ratings = ratings;
        this.runTimes = runTimes;
    }

    // Getters
    public Long getThemeId(){return this.themeId;}
    public Long getVotes(){return this.votes;}
    public String getThemeName(){return this.themeName;}
    public String getSubmitterName(){return this.submitterName;}
    public List<String> getMovieNames(){return this.movieNames;}
    public List<String> getMoviePosters(){return this.moviePosters;}
    public List<String> getDrinkingRules(){return this.drinkingRules;}
    public List<Long> getMovieIds(){return this.movieIds;}
    public List<Double> getRatings(){return this.ratings;}
    public List<Long> getRunTimes(){return this.runTimes;}

    // Setters
    public void setThemeId(Long themeId){this.themeId = themeId;}
    public void setVotes(Long votes){this.votes = votes;}
    public void setThemeName(String themeName){this.themeName = themeName;}
    public void setSubmitterName(String submitterName){this.submitterName = submitterName;}
    public void setMovieNames(List<String> movieNames){this.movieNames = movieNames;}
    public void setMoviePosters(List<String> moviePosters){this.moviePosters = moviePosters;}
    public void setDrinkingRules(List<String> drinkingRules){this.drinkingRules = drinkingRules;}
    public void setMovieIds(List<Long> movieIds){this.movieIds = movieIds;}
    public void setRatings(List<Double> ratings){this.ratings = ratings;}
    public void setRunTimes(List<Long> runTimes){this.runTimes = runTimes;}
}