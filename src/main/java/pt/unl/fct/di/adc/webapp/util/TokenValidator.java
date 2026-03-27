package pt.unl.fct.di.adc.webapp.util;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import pt.unl.fct.di.adc.webapp.enums.Role;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;

import java.util.Arrays;

public class TokenValidator {

    private ApiResponse errorResponse;

    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public TokenValidator() {}

    public Entity validateToken(AuthToken inputToken, Role ... allowedRoles) {

        if (inputToken.getTokenId() == null || inputToken.getTokenId().isBlank()) {
            String codeError = String.valueOf(ErrorCodes.INVALID_TOKEN.getErrorCode());
            String description = ErrorCodes.INVALID_TOKEN.getDescription();

            errorResponse = new ApiResponse(codeError, description);

            return null;
        }

        Key key = datastore.newKeyFactory().setKind("Sessions").newKey(inputToken.getTokenId());

        Entity token =  datastore.get(key);

        if(token == null){
            String codeError = String.valueOf(ErrorCodes.INVALID_TOKEN.getErrorCode());
            String description = ErrorCodes.INVALID_TOKEN.getDescription();

            errorResponse = new ApiResponse(codeError, description);

            return null;
        }

        long expiresAt = token.getLong("expiresAt");
        if(expiresAt < System.currentTimeMillis() / 1000){
            String codeError = String.valueOf(ErrorCodes.TOKEN_EXPIRED.getErrorCode());
            String description = ErrorCodes.TOKEN_EXPIRED.getDescription();

            errorResponse = new ApiResponse(codeError, description);

            return null;
        }

        String role = token.getString("user_role");

        boolean authorized = Arrays.stream(allowedRoles)
                .anyMatch(r -> r.name().equals(role));

        if (!authorized) {
            String codeError = String.valueOf(ErrorCodes.UNAUTHORIZED.getErrorCode());
            String description = ErrorCodes.UNAUTHORIZED.getDescription();

            errorResponse = new ApiResponse(codeError, description);
            return null;
        }
        return token;
    }

    public ApiResponse getErrorResponse() {
        return errorResponse;
    }
}
