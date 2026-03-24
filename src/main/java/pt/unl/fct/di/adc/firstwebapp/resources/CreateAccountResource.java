package pt.unl.fct.di.adc.firstwebapp.resources;


import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.adc.firstwebapp.util.ErrorCodes;
import pt.unl.fct.di.adc.firstwebapp.util.InputRequest;
import pt.unl.fct.di.adc.firstwebapp.util.RegisterData;

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
    public Response createAccount(InputRequest<RegisterData> input) {

        RegisterData data = input.getInput();

        LOG.fine("Creating account: " + data.getUsername());

        if(!data.validRegistration()){
            int codeError = ErrorCodes.INVALID_INPUT.getErrorCode();
            String description = ErrorCodes.INVALID_INPUT.getDescription();

            String errorString = "{\"status\": \"" + codeError + "\", \"data\": \"" + description + "\"}";

            return Response.status(Response.Status.BAD_REQUEST).entity(errorString).type(MediaType.APPLICATION_JSON).build();
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
            int codeError = ErrorCodes.USER_ALREADY_EXISTS.getErrorCode();
            String description = ErrorCodes.USER_ALREADY_EXISTS.getDescription();

            String errorString = "{\"status\": \"" + codeError + "\", \"data\": \"" + description + "\"}";

            return Response.status(Response.Status.BAD_REQUEST).entity(errorString).type(MediaType.APPLICATION_JSON).build();
        }

        String successString = "{\"status\" : \"success\", \"data\":{" +
                "\"username\": \"" + data.getUsername() + "\", \"role\": \""+ data.getRole() + "\"}}";

        return Response.ok().entity(successString).type(MediaType.APPLICATION_JSON).build();
    }
}
