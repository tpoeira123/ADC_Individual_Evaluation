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
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import pt.unl.fct.di.adc.webapp.input.InputRequest;
import pt.unl.fct.di.adc.webapp.response.ResponseResource;
import pt.unl.fct.di.adc.webapp.util.CreateAccountData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * REST endpoint resource for account registration (Op1).
 * Handles the creation of new User entities in the Datastore.
 * This endpoint is public and does not require a token.
 */
@Path("/")
public class CreateAccountResource extends ResponseResource {

    // Logger instance for recording system events, errors, and debugging info for this specific class
    private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public CreateAccountResource() {
    }

    /**
     * Creates a new user account (Op1).
     * @param input Contains the requested username, password, role, phone, and address.
     * @return 200 OK with the created username and role, or appropriate error (e.g., 9901, 9906).
     */
    @POST
    @Path("/createaccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(InputRequest<CreateAccountData> input) {

        CreateAccountData data = input.getInput();

        LOG.fine("Creating account: " + data.getUsername());

        if (!data.validRegistration())
            return errorResponse(ErrorCodes.INVALID_INPUT);

        // Check if the user already exists to prevent overwriting an active account
        Key key = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
        if (datastore.get(key) != null)
            return errorResponse(ErrorCodes.USER_ALREADY_EXISTS);

        // Build the User entity
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
