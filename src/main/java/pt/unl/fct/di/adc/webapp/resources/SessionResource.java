package pt.unl.fct.di.adc.webapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import pt.unl.fct.di.adc.webapp.enums.Role;
import pt.unl.fct.di.adc.webapp.input.InputRequest;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;
import pt.unl.fct.di.adc.webapp.util.AuthToken;
import pt.unl.fct.di.adc.webapp.util.TokenValidator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class SessionResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public SessionResource() {}

    @POST
    @Path("/showauthsessions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response showAuthenticationSessions(InputRequest<Object> input){
        AuthToken inputToken = input.getToken();

        ApiResponse response;

        TokenValidator validate = new TokenValidator();

        Entity token = validate.validateToken(inputToken, Role.ADMIN);

        if ( token == null) {
            return Response.ok(g.toJson(validate.getErrorResponse())).build();
        }

        try{
            Query<Entity> query = Query.newEntityQueryBuilder().setKind("AuthToken").build();

            QueryResults<Entity> sessions = datastore.run(query);

            List<Map<String, Object>> sessionsList = new ArrayList<>();

            while (sessions.hasNext()) {
                Entity user = sessions.next();
                Map<String, Object> sessionsMap = new LinkedHashMap<>();
                sessionsMap.put("tokenId", user.getString("token_id"));
                sessionsMap.put("username", user.getString("user_name"));
                sessionsMap.put("role", user.getString("user_role"));
                sessionsMap.put("expiresAt", user.getLong("expiresAt"));
                sessionsList.add(sessionsMap);
            }

            Map<String, Object> success = new LinkedHashMap<>();
            success.put("sessions", sessionsList);

            response = new ApiResponse("success", success);

            return Response.ok(g.toJson(response)).build();
        }catch (Exception e){
            String codeError = String.valueOf(ErrorCodes.FORBIDDEN.getErrorCode());
            String description = ErrorCodes.FORBIDDEN.getDescription();

            response = new ApiResponse(codeError, description);

            return Response.ok(g.toJson(response)).build();
        }

    }
}
