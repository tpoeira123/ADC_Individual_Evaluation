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
import org.glassfish.jersey.server.model.internal.ResourceMethodDispatcherFactory;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import pt.unl.fct.di.adc.webapp.enums.Role;
import pt.unl.fct.di.adc.webapp.input.InputRequest;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;
import pt.unl.fct.di.adc.webapp.response.ResponseResource;
import pt.unl.fct.di.adc.webapp.util.AuthToken;
import pt.unl.fct.di.adc.webapp.util.LogoutData;
import pt.unl.fct.di.adc.webapp.util.TokenValidator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class LogoutResource extends ResponseResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    public LogoutResource() {
    }

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(InputRequest<LogoutData> input) {

        LOG.fine("Trying to logout " + input.getInput().getUsername() + " of the database");

        TokenValidator validate = new TokenValidator();
        Entity token = validate.validateToken(input.getToken(), Role.ADMIN, Role.BOFFICER, Role.USER);

        if (token == null)
            return Response.ok(g.toJson(validate.getErrorResponse())).build();

        String targetUsername = input.getInput().getUsername();
        String tokenUsername = token.getString("user_name");
        String tokenRole = token.getString("user_role");

        Key keyTargetUser = datastore.newKeyFactory().setKind("User").newKey(targetUsername);
        if (datastore.get(keyTargetUser) == null)
            return errorResponse(ErrorCodes.USER_NOT_FOUND);

        if (!tokenRole.equals(Role.ADMIN.toString()) && !tokenUsername.equals(targetUsername))
            return errorResponse(ErrorCodes.UNAUTHORIZED);

        try {
            if (tokenRole.equals(Role.ADMIN.toString()) && !tokenUsername.equals(targetUsername)) {

                Query<Entity> tokenQuery = Query.newEntityQueryBuilder().setKind("Sessions")
                        .setFilter(StructuredQuery.PropertyFilter.eq("user_name", targetUsername)).build();     // ex: SELECT * FROM Sessions WHERE user_name = 'tp@adc.pt'

                QueryResults<Entity> tokens = datastore.run(tokenQuery);
                while (tokens.hasNext()) {
                    datastore.delete(tokens.next().getKey());
                }
            }
            else
                datastore.delete(token.getKey());

            LOG.fine("User " + input.getInput().getUsername() + " logged out");

            return successResponse(messageData("Logout successful"));
        } catch (Exception e) {
            return errorResponse(ErrorCodes.FORBIDDEN);
        }
    }
}
