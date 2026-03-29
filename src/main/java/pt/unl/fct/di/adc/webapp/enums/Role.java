package pt.unl.fct.di.adc.webapp.enums;

/**
 * Defines the Role Access Control levels for the application.
 */
public enum Role {
    USER, BOFFICER, ADMIN;

    /**
     * Safely converts a String into a Role enum.
     * @param role The string representation of the role (e.g., "admin", "USER")
     * @return The corresponding Role enum, or null if the string does not match any valid role.
     */
    public static Role hasString(String role) {
        try {
            return valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
