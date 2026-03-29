package pt.unl.fct.di.adc.webapp.util;


/**
 * Data Transfer Object for parsing the "input" JSON block during the Logout operation (Op10).
 */
public class LogoutData {
    private String username;

    public LogoutData(){}

    public LogoutData(String username){
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}
