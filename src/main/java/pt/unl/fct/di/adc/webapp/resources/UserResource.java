package pt.unl.fct.di.adc.webapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.adc.webapp.input.InputRequest;
import pt.unl.fct.di.adc.webapp.util.AuthToken;
import pt.unl.fct.di.adc.webapp.util.TokenValidator;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;
import pt.unl.fct.di.adc.webapp.util.UserData;

import java.util.*;
import java.util.logging.Logger;

@Path("/")
public class UserResource {

    private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());

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

        Entity token = validate.validateToken(inputToken);

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


//    @POST
//    @Path("/deleteaccount")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response deleteAccount(InputRequest<UserData> input) {
//        UserData user = input.getInput();
//        AuthToken inputToken = input.getToken();
//
//
//    }

}
