package dk.fklub.fkult.persistence.entities;

import java.io.File;

import dk.fklub.fkult.business.services.shuffleFilter.HasUserId;

public class SoundSample implements HasUserId{
    private String link;
    private String filePath;
    private Long userId;
    private Integer userCount;
    private double userWeight;
    private File file;
    private long id;

    public SoundSample() {}

    public SoundSample(String link, String filePath, Long userId){
        this.link = link;
        this.filePath = filePath;
        this.userId = userId;
    }

    // Get functions
    public String getLink(){
        return this.link;
    }
    public String getFilePath(){
        return this.filePath;
    }
    public Long getUserId(){
        return this.userId;
    }
    public File getFile() {
        return this.file;
    }

    public long getId(){
        return this.id;
    }

    @Override
    public Long getUsersId() {
        return userId;
    }
    @Override
    public Integer getUserCount() {
        return userCount;
    }
    @Override
    public double getUserWeight(){
        return userWeight;
    }

    // Set functions
    public void setLink(String link){
        this.link = link;
    }
    public void setFilePath(String filePath){
        this.filePath = filePath;
    }
    public void setUserId(Long userId){
        this.userId = userId;
    }
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