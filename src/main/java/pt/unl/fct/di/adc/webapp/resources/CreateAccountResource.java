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
import pt.unl.fct.di.adc.webapp.response.ApiResponse;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import pt.unl.fct.di.adc.webapp.input.InputRequest;
import pt.unl.fct.di.adc.webapp.response.ResponseResource;
import pt.unl.fct.di.adc.webapp.util.CreateAccountData;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class CreateAccountResource extends ResponseResource {

    private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public CreateAccountResource() {
    }


    @POST
    @Path("/createaccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(InputRequest<CreateAccountData> input) {

        CreateAccountData data = input.getInput();

        LOG.fine("Creating account: " + data.getUsername());


        if (!data.validRegistration())
            return errorResponse(ErrorCodes.INVALID_INPUT);


        Key key = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());

        if (datastore.get(key) != null)
            return errorResponse(ErrorCodes.USER_ALREADY_EXISTS);


        Entity user = Entity.newBuilder(key).set("user_name", data.getUsername())
                .set("user_pwd", DigestUtils.sha512Hex(data.getPassword()))
                .set("user_phone", data.getPhone())
                .set("user_address", data.getAddress())
                .set("user_role", data.getRole()).build();

        datastore.put(user);

        LOG.fine("User: " + data.getUsername() + " has been created.");

        Map<String, Object> success = new LinkedHashMap<>();
        success.put("username", data.getUsername());
        success.put("role", data.getRole());

        return successResponse(success);
    }
}
