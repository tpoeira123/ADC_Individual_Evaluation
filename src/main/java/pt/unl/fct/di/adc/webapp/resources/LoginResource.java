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
import pt.unl.fct.di.adc.webapp.util.*;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class LoginResource {

    private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public LoginResource() {}

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(InputRequest<LoginData> input) {

        LoginData data = input.getInput();

        ApiResponse response;

        LOG.fine("Login in account: " + data.getUsername());

        if(!data.validLogin()){
            String codeError = String.valueOf(ErrorCodes.INVALID_INPUT.getErrorCode());
            String description = ErrorCodes.INVALID_INPUT.getDescription();

            response = new ApiResponse(codeError, description);

            return Response.ok(g.toJson(response)).build();
        }

        Key key = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());

        Entity user = datastore.get(key);

        if (user == null) {
            String codeError = String.valueOf(ErrorCodes.USER_NOT_FOUND.getErrorCode());
            String description = ErrorCodes.USER_NOT_FOUND.getDescription();

            response = new ApiResponse(codeError, description);

            return Response.ok(g.toJson(response)).build();
        }
        else{

            if(!user.getString("user_pwd").equals(DigestUtils.sha512Hex(data.getPassword()))){
                String codeError = String.valueOf(ErrorCodes.INVALID_CREDENTIALS.getErrorCode());
                String description = ErrorCodes.INVALID_CREDENTIALS.getDescription();

                response = new ApiResponse(codeError, description);

                return Response.ok(g.toJson(response)).build();
            }
            else {
                AuthToken token = new AuthToken(data.getUsername(), user.getString("user_role"));

                Key tokenKey = datastore.newKeyFactory().setKind("AuthToken").newKey(token.getTokenId());

                Entity tokenEntity = Entity.newBuilder(tokenKey).set("user_name", token.getUsername())
                        .set("user_role", token.getRole())
                        .set("issuedAt", token.getIssuedAt())
                        .set("expiresAt", token.getExpiresAt()).build();

                datastore.put(tokenEntity);

                // allows to do the JSON nest without creating a new class
                Map<String, Object> success = new LinkedHashMap<>();
                success.put("tokenId", token.getTokenId());
                success.put("username", token.getUsername());
                success.put("role", token.getRole());
                success.put("issuedAt", token.getIssuedAt());
                success.put("expiresAt", token.getExpiresAt());

                response = new ApiResponse("success", success);

                return Response.ok(g.toJson(response)).build();

            }

        }

    }
}
