package pt.unl.fct.di.adc.webapp.enums;

public enum ErrorCodes {
    INVALID_CREDENTIALS(9900, "The username-password pair is not valid"),
    USER_ALREADY_EXISTS(9901, "Error in creating an account because the username already exists"),
    USER_NOT_FOUND (9902, "The username referred in the operation doesn’t exist in registered accounts"),
    INVALID_TOKEN (9903, "The operation is called with an invalid token (wrong format for example)"),
    TOKEN_EXPIRED (9904, "The operation is called with a token that is expired"),
    UNAUTHORIZED (9905, "The operation is not allowed for the user role"),
    INVALID_INPUT (9906, "The call is using input data not following the correct specification"),
    FORBIDDEN (9907, "The operation generated a forbidden error by other reason");

    private int errorCode;
    private String description;

    ErrorCodes(int errorCode, String description) {
        this.errorCode = errorCode;
        this.description = description;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getDescription() {
        return description;
    }
}
