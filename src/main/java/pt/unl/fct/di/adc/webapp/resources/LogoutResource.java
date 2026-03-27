package pt.unl.fct.di.adc.webapp.resources;


import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jdk.jfr.RecordingState;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import pt.unl.fct.di.adc.webapp.enums.Role;
import pt.unl.fct.di.adc.webapp.input.InputRequest;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;
import pt.unl.fct.di.adc.webapp.util.AuthToken;
import pt.unl.fct.di.adc.webapp.util.LogoutData;
import pt.unl.fct.di.adc.webapp.util.TokenValidator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class LogoutResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public LogoutResource() {}

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(InputRequest<LogoutData> input) {
        LogoutData logoutData = input.getInput();
        String userLoggingOut = logoutData.getUsername();

        AuthToken inputToken = input.getToken();

        ApiResponse response;

        TokenValidator validate = new TokenValidator();

        Entity token = validate.validateToken(inputToken, Role.ADMIN, Role.BOFFICER,  Role.USER);
        if ( token == null) {
            return Response.ok(g.toJson(validate.getErrorResponse())).build();
        }

        Key keyLoggingOut = datastore.newKeyFactory().setKind("User").newKey(userLoggingOut);
        Entity loggingOut = datastore.get(keyLoggingOut);

        if (loggingOut == null) {
            String codeError = String.valueOf(ErrorCodes.USER_NOT_FOUND.getErrorCode());
            String description = ErrorCodes.USER_NOT_FOUND.getDescription();

            response = new ApiResponse(codeError, description);

            return Response.ok(g.toJson(response)).build();
        }

        String userNameToken = token.getString("user_name");
        String userRoleToken = token.getString("user_role");

        if (userRoleToken.equals(Role.USER.toString())) {
            if (!userNameToken.equals(userLoggingOut)) {
                String codeError = String.valueOf(ErrorCodes.UNAUTHORIZED.getErrorCode());
                String description = ErrorCodes.UNAUTHORIZED.getDescription();
                response = new ApiResponse(codeError, description);
                return Response.ok(g.toJson(response)).build();
            }
        } else if (userRoleToken.equals(Role.BOFFICER.toString())) {
            if (!userNameToken.equals(userLoggingOut)) {
                String codeError = String.valueOf(ErrorCodes.UNAUTHORIZED.getErrorCode());
                String description = ErrorCodes.UNAUTHORIZED.getDescription();
                response = new ApiResponse(codeError, description);
                return Response.ok(g.toJson(response)).build();
            }
        }
        try {
            Query<Entity> tokenQuery = Query.newEntityQueryBuilder().setKind("Sessions")
                    .setFilter(StructuredQuery.PropertyFilter.eq("user_name", userLoggingOut)).build();     // ex: SELECT * FROM Sessions WHERE user_name = 'tp@adc.pt'

            QueryResults<Entity> tokens = datastore.run(tokenQuery);

            while (tokens.hasNext()) {
                datastore.delete(tokens.next().getKey());
            }

            Map<String, Object> success = new LinkedHashMap<>();
            success.put("message", "Logout successful");

            response = new ApiResponse("success", success);

            return Response.ok(g.toJson(response)).build();
        }catch (Exception e) {
            String codeError = String.valueOf(ErrorCodes.FORBIDDEN.getErrorCode());
            String description = ErrorCodes.FORBIDDEN.getDescription();
            response = new ApiResponse(codeError, description);
            return Response.ok(g.toJson(response)).build();
        }
    }
}
