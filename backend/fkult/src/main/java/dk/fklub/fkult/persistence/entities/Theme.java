package dk.fklub.fkult.persistence.entities;

import java.time.LocalDateTime;


import dk.fklub.fkult.business.services.shuffleFilter.HasUserId;

public class Theme implements HasUserId {

    private long id;
    private String name;
    private long userid;
    private LocalDateTime timestamp;
    private Integer votecount;
    private Integer userCount;
    private double userWeight;

    public Theme (long id, String name, Long userid, LocalDateTime timestamp, Integer votecount){
        this.id = id;
        this.name = name;
        this.userid = userid;
        this.timestamp = timestamp;
        this.votecount = votecount;
    }
    public Theme(String name, long userid){
        this.name = name;
        this.userid = userid;
    }

    //getters
    public long getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
    public long getUserid(){
        return this.userid;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }
    public Integer getVotecount(){
        return this.votecount;
    }

    @Override
    public Long getUsersId(){
        return userid;
    }
    @Override
    public Integer getUserCount(){
        return userCount;
    }
    @Override
    public double getUserWeight(){
        return userWeight;
    }

    //setters
    public void setId(long id){
        this.id = id;
    }

    @Override
    public void setUserCount(Integer count){
        this.userCount = count;
    }
    @Override
    public void setUserWeight(double weight){
        this.userWeight = weight;
    }
}
