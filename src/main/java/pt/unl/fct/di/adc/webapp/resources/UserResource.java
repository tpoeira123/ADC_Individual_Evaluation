package pt.unl.fct.di.adc.webapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import pt.unl.fct.di.adc.webapp.enums.Role;
import pt.unl.fct.di.adc.webapp.input.InputRequest;
import pt.unl.fct.di.adc.webapp.response.ResponseResource;
import pt.unl.fct.di.adc.webapp.util.*;
import pt.unl.fct.di.adc.webapp.response.ApiResponse;
import com.google.cloud.datastore.StructuredQuery.*;

import java.util.*;
import java.util.logging.Logger;

@Path("/")
public class UserResource extends ResponseResource {

    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public UserResource() {
    }


    @POST
    @Path("/showusers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response showUsers(InputRequest<Object> input) {

        LOG.fine("Trying to show users in the database");

        TokenValidator validate = new TokenValidator();
        Entity token = validate.validateToken(input.getToken(), Role.ADMIN, Role.BOFFICER);

        if (token == null)
            return Response.ok(g.toJson(validate.getErrorResponse())).build();

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

        LOG.fine("Showing users");

        Map<String, Object> success = new LinkedHashMap<>();
        success.put("users", usersList);

        return successResponse(success);
    }


    @POST
    @Path("/deleteaccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAccount(InputRequest<UserData> input) {

        LOG.fine("Trying to delete account " + input.getInput().getUsername() + "of the database");

        TokenValidator validate = new TokenValidator();
        Entity token = validate.validateToken(input.getToken(), Role.ADMIN);

        if (token == null)
            return Response.ok(g.toJson(validate.getErrorResponse())).build();

        String tokenUsername = token.getString("user_name");
        String targetUsername = input.getInput().getUsername();

        Key keyTargetUser = datastore.newKeyFactory().setKind("User").newKey(targetUsername);
        Entity targetUser = datastore.get(keyTargetUser);

        if (targetUser == null)
            return errorResponse(ErrorCodes.USER_NOT_FOUND);

        String targetedRole = targetUser.getString("user_role");
        if (!targetUsername.equals(tokenUsername) && targetedRole.equals(Role.ADMIN.toString()))
            return errorResponse(ErrorCodes.UNAUTHORIZED);

        try {
            datastore.delete(keyTargetUser);

            Query<Entity> tokenQuery = Query.newEntityQueryBuilder().setKind("Sessions")
                    .setFilter(PropertyFilter.eq("user_name", targetUsername)).build();     // ex: SELECT * FROM Sessions WHERE user_name = 'tp@adc.pt'

            QueryResults<Entity> tokens = datastore.run(tokenQuery);
            while (tokens.hasNext()) {
                datastore.delete(tokens.next().getKey());
            }

            LOG.fine("Account " + input.getInput().getUsername() + "deleted, along side is tokens");

            return successResponse(messageData("Account deleted successfully"));
        } catch (Exception e) {
            return errorResponse(ErrorCodes.FORBIDDEN);
        }
    }

    @POST
    @Path("/modaccount")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyAccount(InputRequest<UserData> input) {

        LOG.fine("Trying to modify account " + input.getInput().getUsername() + "of the database");

        TokenValidator validate = new TokenValidator();
        Entity token = validate.validateToken(input.getToken(), Role.ADMIN, Role.BOFFICER, Role.USER);

        if (token == null)
            return Response.ok(g.toJson(validate.getErrorResponse())).build();

        UserData inputUser = input.getInput();
        AttributesData attributes = inputUser.getAttributes();

        if (attributes == null)
            return errorResponse(ErrorCodes.INVALID_INPUT);

        Key keyTargetUser = datastore.newKeyFactory().setKind("User").newKey(inputUser.getUsername());
        Entity targetUser = datastore.get(keyTargetUser);

        if (targetUser == null)
            return errorResponse(ErrorCodes.USER_NOT_FOUND);

        String modifyingUsername = token.getString("user_name");
        String modifyingRole = token.getString("user_role");

        String targetedUsername = targetUser.getString("user_name");
        String targetedRole = targetUser.getString("user_role");

        if (modifyingRole.equals(Role.USER.toString()) && !modifyingUsername.equals(targetedUsername))
            return errorResponse(ErrorCodes.UNAUTHORIZED);

        else if (modifyingRole.equals(Role.BOFFICER.toString()) && !modifyingUsername.equals(targetedUsername)
                && !targetedRole.equals(Role.USER.toString()))
            return errorResponse(ErrorCodes.UNAUTHORIZED);


        try {
            Entity.Builder updatedUser = Entity.newBuilder(targetUser);
            if (attributes.getAddress() != null && !attributes.getAddress().isBlank())
                updatedUser.set("user_address", attributes.getAddress());

            if (attributes.getPhone() != null && !attributes.getPhone().isBlank())
                updatedUser.set("user_phone", attributes.getPhone());

            datastore.put(updatedUser.build());

            LOG.fine("Account " + targetedUsername + "modified");

            return successResponse(messageData("Updated successfully"));
        } catch (Exception e) {
            return errorResponse(ErrorCodes.FORBIDDEN);
        }
    }

    @POST
    @Path("/showuserrole")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response showUserRoles(InputRequest<UserData> input) {

        LOG.fine("Trying to show user roles in the database");

        TokenValidator validate = new TokenValidator();
        Entity token = validate.validateToken(input.getToken(), Role.ADMIN, Role.BOFFICER);
        if (token == null)
            return Response.ok(g.toJson(validate.getErrorResponse())).build();

        Key keyTargetUser = datastore.newKeyFactory().setKind("User").newKey(input.getInput().getUsername());
        Entity targetUser = datastore.get(keyTargetUser);

        if (targetUser == null)
            return errorResponse(ErrorCodes.USER_NOT_FOUND);

        try {
            Map<String, Object> success = new LinkedHashMap<>();
            success.put("username", targetUser.getString("user_name"));
            success.put("role", targetUser.getString("user_role"));

            LOG.fine("Showing user roles in the database");

            return successResponse(success);
        } catch (Exception e) {
            return errorResponse(ErrorCodes.FORBIDDEN);
        }
    }


    @POST
    @Path("/changeuserrole")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeUserRoles(InputRequest<UserData> input) {

        LOG.fine("Trying to change " + input.getInput().getUsername() + " role in the database to " + input.getInput().getNewRole());

        String newRole = input.getInput().getNewRole();

        if (Role.hasString(newRole) == null)
            return errorResponse(ErrorCodes.INVALID_INPUT);

        TokenValidator validate = new TokenValidator();
        Entity token = validate.validateToken(input.getToken(), Role.ADMIN);
        if (token == null)
            return Response.ok(g.toJson(validate.getErrorResponse())).build();

        String targetUsername = input.getInput().getUsername();

        Key keyTargetUser = datastore.newKeyFactory().setKind("User").newKey(targetUsername);
        Entity targetUser = datastore.get(keyTargetUser);

        if (targetUser == null)
            return errorResponse(ErrorCodes.USER_NOT_FOUND);

        String tokenUsername =  token.getString("user_name");
        if (input.getInput().getUsername().equals(tokenUsername))
            return errorResponse(ErrorCodes.FORBIDDEN);

        try {
            Entity.Builder updatedUser = Entity.newBuilder(targetUser);
            updatedUser.set("user_role", newRole);

            datastore.put(updatedUser.build());

            Query<Entity> tokenQuery = Query.newEntityQueryBuilder().setKind("Sessions")
                    .setFilter(PropertyFilter.eq("user_name", targetUsername)).build();     // ex: SELECT * FROM Sessions WHERE user_name = 'tp@adc.pt'

            QueryResults<Entity> tokens = datastore.run(tokenQuery);
            while (tokens.hasNext()) {
                Entity existingToken = tokens.next();
                Entity updatedToken = Entity.newBuilder(existingToken).set("user_role", newRole).build();

                datastore.put(updatedToken);
            }

            LOG.fine("Changed " + input.getInput().getUsername() + " role in the database to " + input.getInput().getNewRole());

            return successResponse(messageData("Role updated successfully"));

        } catch (Exception e) {
            return  errorResponse(ErrorCodes.FORBIDDEN);
        }
    }


    @POST
    @Path("/changeuserpwd")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeUserPassword(InputRequest<UserData> input) {

        LOG.fine("Trying to change " + input.getInput().getUsername() + " password");

        TokenValidator validate = new TokenValidator();
        Entity token = validate.validateToken(input.getToken(), Role.ADMIN, Role.BOFFICER, Role.USER);

        if (token == null)
            return Response.ok(g.toJson(validate.getErrorResponse())).build();

        UserData inputUser = input.getInput();
        String targetUsername = inputUser.getUsername();
        String tokenUsername = token.getString("user_name");

        if (!tokenUsername.equals(targetUsername))
            return errorResponse(ErrorCodes.UNAUTHORIZED);

        Key keyTargetUser = datastore.newKeyFactory().setKind("User").newKey(targetUsername);
        Entity targetUser = datastore.get(keyTargetUser);

        String oldPassword = inputUser.getOldPassword();
        String newPassword = inputUser.getNewPassword();

        if (targetUser == null)
            return errorResponse(ErrorCodes.USER_NOT_FOUND);
        else if (!targetUser.getString("user_pwd").equals(DigestUtils.sha512Hex(oldPassword)))
            return errorResponse(ErrorCodes.INVALID_CREDENTIALS);

        if (newPassword == null || newPassword.isBlank())
            return errorResponse(ErrorCodes.INVALID_INPUT);

        try {
            Entity.Builder updatedUser = Entity.newBuilder(targetUser);
            updatedUser.set("user_pwd", DigestUtils.sha512Hex(newPassword));

            datastore.put(updatedUser.build());

            LOG.fine("Password of " + input.getInput().getUsername() + " changed");

            return successResponse(messageData("Password changed successfully"));
        } catch (Exception e) {
            return errorResponse(ErrorCodes.FORBIDDEN);
        }
    }
}
