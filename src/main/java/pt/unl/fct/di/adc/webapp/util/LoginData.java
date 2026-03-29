package pt.unl.fct.di.adc.webapp.util;


/**
 * Data Transfer Object for parsing the "input" JSON block during the Login operation (Op2).
 */
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

    /**
     * Helper method to ensure a string is neither null nor completely empty.
     */
    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

    /**
     * Validates that the client provided both necessary login fields before attempting to query the database.
     */
    public boolean validLogin() {
        return nonEmptyOrBlankField(username) && nonEmptyOrBlankField(password);
    }

}
