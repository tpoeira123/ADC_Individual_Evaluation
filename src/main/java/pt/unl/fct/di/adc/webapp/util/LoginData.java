package pt.unl.fct.di.adc.webapp.util;

public class LoginData {

    private String username;
    private String password;

    public LoginData(){}

    public LoginData(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }


    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

    public boolean validLogin() {
        return nonEmptyOrBlankField(username) && nonEmptyOrBlankField(password);
    }

}
