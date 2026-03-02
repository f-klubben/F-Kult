package dk.fklub.fkult.business.services.shuffleFilter;

public interface HasUserId {
    Long getUsersId();
    Integer getUserCount();
    double getUserWeight();
    void setUserCount(Integer count);
    void setUserWeight(double weight);
}