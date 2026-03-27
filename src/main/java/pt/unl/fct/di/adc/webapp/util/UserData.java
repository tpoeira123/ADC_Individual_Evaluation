package pt.unl.fct.di.adc.webapp.util;

public class UserData {
    private String username;
    private String oldPassword;
    private String newPassword;
    private String newRole;
    private AttributesData attributes;

    public UserData() {}

    public UserData(String username){
        this.username = username;
    }

    public UserData(String username, AttributesData attributes){
        this.username = username;
        this.attributes = attributes;
    }

    public UserData(String username, String newRole){
        this.username = username;
        this.newRole = newRole;
    }

    public UserData(String username, String oldPassword, String newPassword){
        this.username = username;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
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

    public String getNewRole() {
        return newRole;
    }
    public void setNewRole(String newRole) {
        this.newRole = newRole;
    }

    public String getOldPassword() {
        return oldPassword;
    }
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
