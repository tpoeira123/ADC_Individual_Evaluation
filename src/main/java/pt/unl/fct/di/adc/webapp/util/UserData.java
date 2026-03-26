package pt.unl.fct.di.adc.webapp.util;

public class UserData {
    private String username;
    private AttributesData attributes;

    public UserData() {}

    public UserData(String username){
        this.username = username;
    }

    public UserData(String username, AttributesData attributes){
        this.username = username;
        this.attributes = attributes;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public AttributesData getAttributes() {
        return attributes;
    }
    public void setAttributes(AttributesData attributes) {
        this.attributes = attributes;
    }
}
