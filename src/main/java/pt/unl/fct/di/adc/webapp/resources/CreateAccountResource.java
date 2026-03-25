package pt.unl.fct.di.adc.webapp.resources;


import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import response.ApiResponse;
import pt.unl.fct.di.adc.webapp.util.ErrorCodes;
import pt.unl.fct.di.adc.webapp.util.InputRequest;
import pt.unl.fct.di.adc.webapp.util.CreateAccountData;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class CreateAccountResource {

    private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public CreateAccountResource() {}


    @POST
    @Path("/createaccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(InputRequest<CreateAccountData> input) {

        CreateAccountData data = input.getInput();

        LOG.fine("Creating account: " + data.getUsername());

        ApiResponse response;

        if(!data.validRegistration()){
            String codeError = String.valueOf(ErrorCodes.INVALID_INPUT.getErrorCode());
            String description = ErrorCodes.INVALID_INPUT.getDescription();

            response = new ApiResponse(codeError, description);

            return Response.status(Response.Status.BAD_REQUEST).entity(response).type(MediaType.APPLICATION_JSON).build();
        }

        Key key = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());

        Entity user = datastore.get(key);

        if (user == null){
            user = Entity.newBuilder(key).set("user_name", data.getUsername())
                    .set("user_pwd", DigestUtils.sha512Hex(data.getPassword()))
                    .set("user_email", data.getEmail())
                    .set("user_phone", data.getPhone())
                    .set("user_address", data.getAddress())
                    .set("user_role", data.getRole())
                    .build();

            datastore.put(user);
            LOG.fine("User: " + data.getUsername() + " has been created.");
        }
        else {
            String codeError = String.valueOf(ErrorCodes.USER_ALREADY_EXISTS.getErrorCode());
            String description = ErrorCodes.USER_ALREADY_EXISTS.getDescription();

            response = new ApiResponse(codeError, description);

            return Response.status(Response.Status.BAD_REQUEST).entity(response).type(MediaType.APPLICATION_JSON).build();
        }

        Map<String, Object> success = new LinkedHashMap<>();
        success.put("username", data.getUsername());
        success.put("role", data.getRole());

        response = new ApiResponse("success", success);

        return Response.ok().entity(response).type(MediaType.APPLICATION_JSON).build();
    }
}
