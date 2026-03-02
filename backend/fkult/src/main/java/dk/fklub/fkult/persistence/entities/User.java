package dk.fklub.fkult.persistence.entities;

public class User {
    private long id;
    private String username;
    private String name;
    private int banned;
    private int admin;

    //constructor
    public User(long id, String username, String name, int banned, int admin){
        this.id = id;
        this.username = username;
        this.name = name;
        this.banned = banned;
        this.admin = admin;
    }

    //getters
    public long getId(){
        return this.id;
    }
    public String getUsername(){
        return this.username;
    }
    public String getName(){
        return this.name;
    }
    public int getBanned() {
        return this.banned;
    }
    public int getAdmin(){
        return this.admin;
    }

    //setters
    public void setId(long value){
        this.id = value;
    }
    public void setUsername(String value){
        this.username = value;
    }
    public void setName(String value){
        this.name = value;
    }
    public void setBanned(int value) {
        this.banned = value;
    }
    public void setAdmin(int value){
        this.admin = value;
    }

}
