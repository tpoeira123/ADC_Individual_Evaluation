package pt.unl.fct.di.adc.webapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.checkerframework.checker.units.qual.K;
import pt.unl.fct.di.adc.webapp.input.InputRequest;
import pt.unl.fct.di.adc.webapp.util.*;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;
import com.google.cloud.datastore.StructuredQuery.*;

import java.util.*;
import java.util.logging.Logger;

@Path("/")
public class UserResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public UserResource() {}

    @POST
    @Path("/showusers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response showUsers(InputRequest<Object> input) {
        AuthToken inputToken = input.getToken();

        ApiResponse response;

        TokenValidator validate = new TokenValidator();

        Entity token = validate.validateToken(inputToken, Role.BOFFICER, Role.ADMIN);

        if ( token == null) {
            return Response.ok(g.toJson(validate.getErrorResponse())).build();
        }

        Query<Entity> query = Query.newEntityQueryBuilder().setKind("User").build();

        QueryResults<Entity> users = datastore.run(query);

        List<Map<String, Object>> usersList = new ArrayList<>();

        while (users.hasNext()) {
            Entity user = users.next();
            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("username", user.getString("user_name"));
            userMap.put("role", user.getString("user_role"));
            usersList.add(userMap);
        }

        Map<String, Object> success = new LinkedHashMap<>();
        success.put("users", usersList);

        response = new ApiResponse("success", success);

        return Response.ok(g.toJson(response)).build();
    }


    @POST
    @Path("/deleteaccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAccount(InputRequest<UserData> input) {
        UserData inputUser = input.getInput();
        AuthToken inputToken = input.getToken();
        ApiResponse response;

        TokenValidator validate = new TokenValidator();
        Entity token = validate.validateToken(inputToken, Role.ADMIN);
        if ( token == null) {
            return Response.ok(g.toJson(validate.getErrorResponse())).build();
        }

        Key key = datastore.newKeyFactory().setKind("User").newKey(token.getString("user_name"));
        Entity userDeleting = datastore.get(key);

        Key keyTargetUser =  datastore.newKeyFactory().setKind("User").newKey(inputUser.getUsername());
        Entity targetUser =  datastore.get(keyTargetUser);

        if (targetUser == null) {
            String codeError = String.valueOf(ErrorCodes.USER_NOT_FOUND.getErrorCode());
            String description = ErrorCodes.USER_NOT_FOUND.getDescription();

            response = new ApiResponse(codeError, description);

            return Response.ok(g.toJson(response)).build();
        }

        String deletingUsername = userDeleting.getString("user_name");
        String targetedUsername = targetUser.getString("user_name");
        String targetedRole = targetUser.getString("user_role");

        if (!targetedUsername.equals(deletingUsername) && targetedRole.equals(Role.ADMIN.toString())) {
            String codeError = String.valueOf(ErrorCodes.FORBIDDEN.getErrorCode());
            String description = ErrorCodes.FORBIDDEN.getDescription();

            response = new ApiResponse(codeError, description);

            return Response.ok(g.toJson(response)).build();
        }

        datastore.delete(keyTargetUser);

        Query<Entity> tokenQuery = Query.newEntityQueryBuilder().setKind("AuthToken")
                .setFilter(PropertyFilter.eq("user_name", inputUser.getUsername())).build();     // ex: SELECT * FROM AuthToken WHERE user_name = 'tp@adc.pt'

        QueryResults<Entity> tokens = datastore.run(tokenQuery);

        while (tokens.hasNext()) {
            datastore.delete(tokens.next().getKey());
        }

        Map<String, Object> success = new LinkedHashMap<>();
        success.put("data", "Account deleted successfully");

        response = new ApiResponse("success", success);

        return Response.ok(g.toJson(response)).build();
    }

}
