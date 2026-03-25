package pt.unl.fct.di.adc.webapp.util;

public enum Role {
    USER, BOFFICER, ADMIN;

    // valueOf() throws an exception if the String passed doesn't match with the Role Enums, so, if the exception occurs,
    // we catch the exception and return null
    public static Role hasString(String role) {
        try {
            return valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
