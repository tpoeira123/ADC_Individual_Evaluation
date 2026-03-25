package pt.unl.fct.di.adc.webapp.util;

public class UserData {
    private String username;

    public UserData() {}

    public UserData(String username){
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
