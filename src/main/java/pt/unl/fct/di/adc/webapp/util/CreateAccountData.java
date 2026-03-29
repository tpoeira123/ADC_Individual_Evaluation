package pt.unl.fct.di.adc.webapp.util;

import pt.unl.fct.di.adc.webapp.enums.Role;


/**
 * Data Transfer Object for parsing the "input" JSON block during the Create Account operation (Op1).
 */
public class CreateAccountData {

    private String username;
    private String password;
    private String confirmation;
    private String phone;
    private String address;
    private String role;


    public CreateAccountData() {}

    public CreateAccountData(String username, String password, String confirmation,
                             String phone, String address, String role) {

        this.username = username;
        this.password = password;
        this.confirmation = confirmation;
        this.phone = phone;
        this.address = address;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getRole() {
        return role.toUpperCase();
    }

    /**
     * Helper method to ensure a string is neither null nor completely empty.
     */
    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

    /**
     * Executes business logic validation on the incoming registration data.
     * @return true if all fields are present, passwords match, username is an email, and the role is valid.
     */
    public boolean validRegistration() {
        return nonEmptyOrBlankField(username) && nonEmptyOrBlankField(password) &&
                nonEmptyOrBlankField(phone) && nonEmptyOrBlankField(address) &&
                username.contains("@") && password.equals(confirmation) && Role.hasString(role) != null;
    }
}
