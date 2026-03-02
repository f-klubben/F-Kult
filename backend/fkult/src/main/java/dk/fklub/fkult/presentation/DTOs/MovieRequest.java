package dk.fklub.fkult.presentation.DTOs;

public class MovieRequest {
    private long movieId;
    private String tConst;
    private String title;
    private String moviePosterURL;
    private int runtimeMinutes;
    private int year;
    private String rating;
    private boolean isSeries;
    private boolean isShorts;


    //constructor
    public MovieRequest(long movieId, String title, String moviePosterURL, String rating, int runtimeMinutes, int year){
        this.movieId = movieId;
        this.title = title;
        this.moviePosterURL = moviePosterURL;
        this.runtimeMinutes = runtimeMinutes;
        this.year = year;
        this.rating = rating;
    }
    public MovieRequest(String tConst, String title, int runtimeMinutes, int year, String rating, String moviePosterURL, boolean isSeries, boolean isShorts){
        this.tConst = tConst;
        this.title = title;
        this.runtimeMinutes = runtimeMinutes;
        this.year = year;
        this.moviePosterURL = moviePosterURL;
        this.isSeries = isSeries;
        this.isShorts = isShorts;
        this.rating = rating;
    }

    //setters
    public void setMovieId(long movieId){
        this.movieId = movieId;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setMoviePosterURL(String moviePosterURL){
        this.moviePosterURL = moviePosterURL;
    }
    public void setRating(String rating){
        this.rating = rating;
    }
    public void setRuntimeMinutes(int runtimeMinutes){
        this.runtimeMinutes = runtimeMinutes;
    }
    public void setYear(int year){
        this.year = year;
    }
    public void settConst(String tConst){this.tConst = tConst;}
    public void setIsSeries(boolean isSeries){this.isSeries = isSeries;}
    public void setIsShorts(boolean isShorts){this.isShorts = isShorts;}

    //getters
    public long getMovieId(){
        return movieId;
    }
    public String getTitle(){
        return title;
    }
    public String getMoviePosterURL(){
        return moviePosterURL;
    }
    public int getRuntimeMinutes(){
        return runtimeMinutes;
    }
    public int getYear(){
        return year;
    }
    public String getRating(){
        return rating;
    }
    public String gettConst(){return tConst;}
    public boolean getIsSeries(){return isSeries;}
    public boolean getIsShorts(){return isShorts;}
}