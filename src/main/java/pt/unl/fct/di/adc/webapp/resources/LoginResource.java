package pt.unl.fct.di.adc.webapp.resources;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import pt.unl.fct.di.adc.webapp.input.InputRequest;
import pt.unl.fct.di.adc.webapp.response.ResponseResource;
import pt.unl.fct.di.adc.webapp.util.*;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class LoginResource extends ResponseResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public LoginResource() {
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(InputRequest<LoginData> input) {

        LoginData data = input.getInput();

        LOG.fine("Login attempt: " + data.getUsername());

        if (!data.validLogin())
            return errorResponse(ErrorCodes.INVALID_INPUT);

        Key key = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
        Entity user = datastore.get(key);

        if (user == null)
            return errorResponse(ErrorCodes.USER_NOT_FOUND);


        if (!user.getString("user_pwd").equals(DigestUtils.sha512Hex(data.getPassword())))
            return errorResponse(ErrorCodes.INVALID_CREDENTIALS);


        AuthToken token = new AuthToken(data.getUsername(), user.getString("user_role"));

        Key tokenKey = datastore.newKeyFactory().setKind("Sessions").newKey(token.getTokenId());

        Entity tokenEntity = Entity.newBuilder(tokenKey).set("token_id", token.getTokenId())
                .set("user_name", token.getUsername())
                .set("user_role", token.getRole())
                .set("issuedAt", token.getIssuedAt())
                .set("expiresAt", token.getExpiresAt()).build();

        datastore.put(tokenEntity);

        LOG.fine("New login created with id: " + token.getTokenId());

        // allows to do the JSON nest without creating a new class
        Map<String, Object> displayToken = new LinkedHashMap<>();
        displayToken.put("tokenId", token.getTokenId());
        displayToken.put("username", token.getUsername());
        displayToken.put("role", token.getRole());
        displayToken.put("issuedAt", token.getIssuedAt());
        displayToken.put("expiresAt", token.getExpiresAt());

        Map<String, Object> success = new LinkedHashMap<>();
        success.put("token", displayToken);

        return successResponse(success);
    }
}
