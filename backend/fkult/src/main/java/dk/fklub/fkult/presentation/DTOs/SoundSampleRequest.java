package dk.fklub.fkult.presentation.DTOs;

public class SoundSampleRequest {
    private String usersFullName;
    private String username;
    private String soundSample;
    private long id;
    
    public SoundSampleRequest(String soundSample, String username, String usersFullName, long id) {
        this.soundSample = soundSample;
        this.username = username;
        this.usersFullName = usersFullName;
        this.id = id;
    }

    // Getters
    public String getSoundSample() {
        return soundSample;
    }
    public String getUsername() {
        return username;
    }
    public String getUsersFullName() {
        return usersFullName;
    }
    public Long getId(){
        return id;
    }

    // Setters
    public void setSoundSample(String soundSample) {
        this.soundSample = soundSample;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setId(long id){
        this.id = id;
    }
}
