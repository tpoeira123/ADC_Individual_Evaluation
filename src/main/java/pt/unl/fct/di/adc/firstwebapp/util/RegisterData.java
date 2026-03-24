package pt.unl.fct.di.adc.firstwebapp.util;

public class RegisterData {

    private String username;
    private String password;
    private String confirmation;
    private String email;
    private String phone;
    private String address;
    private String role;


    public RegisterData() {}

    public RegisterData(String username, String password, String confirmation, String email,
                         String phone, String address, String role) {

        this.username = username;
        this.password = password;
        this.confirmation = confirmation;
        this.email = email;
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

    public String getEmail() {
        return email;
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

    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

    public boolean validRegistration() {
        return nonEmptyOrBlankField(username) && nonEmptyOrBlankField(password) &&
                nonEmptyOrBlankField(email) && nonEmptyOrBlankField(phone) &&
                nonEmptyOrBlankField(address) && email.contains("@") &&
                password.equals(confirmation) && Role.hasString(role) != null;
    }
}
