package dk.fklub.fkult.persistence.entities;

public class ThemeMovie {
    private long themeid;
    private long movieid;

    //constructor
    public ThemeMovie(long themeid, long movieid){
        this.themeid = themeid;
        this.movieid = movieid;
    }

    //setters
    public void setThemeid(long themeid){
        this.themeid = themeid;
    }
    public void setMovieid(long movieid){
        this.movieid = movieid;
    }
    //getters
    public long getThemeid(){
        return this.themeid;
    }
    public long getMovieid(){
        return this.movieid;
    }
}
