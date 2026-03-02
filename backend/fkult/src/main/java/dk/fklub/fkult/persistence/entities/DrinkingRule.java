package dk.fklub.fkult.persistence.entities;

public class DrinkingRule {
    private long id;
    private long themeId;
    private String ruleText;

    //constructors
    public DrinkingRule(long id, long themeId, String ruleText){
        this.id = id;
        this.themeId = themeId;
        this.ruleText = ruleText;
    }

    public DrinkingRule(long themeId, String ruleText){
        this.themeId = themeId;
        this.ruleText = ruleText;
    }

    //setters
    public void setId(long id){
        this.id = id;
    }
    public void setThemeId(long themeId){
        this.themeId = themeId;
    }
    public void setRuleText(String ruleText){
        this.ruleText = ruleText;
    }

    //getters
    public long getId(){
        return this.id;
    }
    public long getThemeId(){
        return this.themeId;
    }
    public String getRuleText(){
        return this.ruleText;
    }
}