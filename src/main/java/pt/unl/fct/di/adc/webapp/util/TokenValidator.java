package pt.unl.fct.di.adc.webapp.util;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import pt.unl.fct.di.adc.webapp.enums.Role;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;
import java.util.Arrays;


/**
 * Security utility class responsible for intercepting and validating tokens.
 * Checks token formatting, Datastore existence, expiration, and enforces Roles that are authorized in a method.
 */
public class TokenValidator {

    private ApiResponse errorResponse;
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public TokenValidator() {}

    /**
     * Executes a validation check on an incoming authentication token.
     * @param inputToken The token parsed from the incoming JSON request.
     * @param allowedRoles A dynamic list of roles allowed to access the specific endpoint.
     * @return The Datastore token Entity if valid, or null if validation fails (sets errorResponse).
     */
    public Entity validateToken(AuthToken inputToken, Role ... allowedRoles) {
        // Format Check - Ensure a token was actually provided
        if (inputToken.getTokenId() == null || inputToken.getTokenId().isBlank()) {
            String codeError = String.valueOf(ErrorCodes.INVALID_TOKEN.getErrorCode());
            String description = ErrorCodes.INVALID_TOKEN.getDescription();

            errorResponse = new ApiResponse(codeError, description);
            return null;
        }

        // Verify the token is in the server's Datastore
        Key key = datastore.newKeyFactory().setKind("Sessions").newKey(inputToken.getTokenId());
        Entity token =  datastore.get(key);

        if(token == null){
            String codeError = String.valueOf(ErrorCodes.INVALID_TOKEN.getErrorCode());
            String description = ErrorCodes.INVALID_TOKEN.getDescription();

            errorResponse = new ApiResponse(codeError, description);
            return null;
        }

        // Verify the token hasn't passed its 15-minute lifespan
        // Clean up the expired token automatically to save database space
        long expiresAt = token.getLong("expiresAt");
        if(expiresAt < System.currentTimeMillis() / 1000){
            String codeError = String.valueOf(ErrorCodes.TOKEN_EXPIRED.getErrorCode());
            String description = ErrorCodes.TOKEN_EXPIRED.getDescription();

            errorResponse = new ApiResponse(codeError, description);
            datastore.delete(token.getKey());
            return null;
        }

        // Verify the token owner's role matches the endpoint allowed Roles
        String role = token.getString("user_role");
        // Checks if the user's role exists within the array of allowed roles for this endpoint
        boolean authorized = Arrays.stream(allowedRoles).anyMatch(r -> r.name().equals(role));

        if (!authorized) {
            String codeError = String.valueOf(ErrorCodes.UNAUTHORIZED.getErrorCode());
            String description = ErrorCodes.UNAUTHORIZED.getDescription();

            errorResponse = new ApiResponse(codeError, description);
            return null;
        }
        return token;
    }

    /**
     * Retrieves the generated error response if validation fails.
     */
    public ApiResponse getErrorResponse() {
        return errorResponse;
    }
}
