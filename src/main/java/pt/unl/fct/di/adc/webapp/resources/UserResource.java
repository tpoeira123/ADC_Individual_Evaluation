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
import com.google.cloud.datastore.StructuredQuery.*;
import java.util.*;
import java.util.logging.Logger;


/**
 * REST endpoint resource for managing User entities (Op3, Op4, Op5, Op7, Op8, Op9).
 * Handles fetching, modifying, deleting users, and changing roles and passwords.
 */
@Path("/")
public class UserResource extends ResponseResource {

    // Logger instance for recording system events, errors, and debugging info for this specific class
    private static final Logger LOG = Logger.getLogger(UserResource.class.getName());

    // converts an object of java to json format or vice versa
    private final Gson g = new Gson();

    // connects the application to a database in Google Cloud
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public UserResource() {
    }

    /**
     * Lists all registered users (Op3).
     * The only roles that can perform this method are ADMIN and BOFFICER.
     * @param input Contains the Token.
     * @return 200 OK, showing the users of the database, or appropriate error (e.g., 9901, 9906).
     */
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

        // Find all users in the User entity and put them in a list so it can be sent by JSON
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


    /**
     * Deletes a user account and all their active sessions (Op4).
     * The only role that can perform this method is ADMIN.
     * @param input Contains the username we want to delete and the Token.
     * @return 200 OK with deleting an account, or appropriate error (e.g., 9901, 9906).
     */
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

        try {
            // Delete the main user entity
            datastore.delete(keyTargetUser);

            // Find and remove all session tokens for this user
            Query<Entity> tokenQuery = Query.newEntityQueryBuilder().setKind("Sessions")
                    .setFilter(PropertyFilter.eq("user_name", targetUsername)).build();     // ex: SELECT * FROM Sessions WHERE user_name = 'tp@adc.pt'

            QueryResults<Entity> tokens = datastore.run(tokenQuery);
            while (tokens.hasNext()) {
                datastore.delete(tokens.next().getKey());
            }

            LOG.fine("Account " + input.getInput().getUsername() + "deleted, along side is tokens, by " + tokenUsername);

            return successResponse(messageData("Account deleted successfully"));
        } catch (Exception e) {
            return errorResponse(ErrorCodes.FORBIDDEN);
        }
    }


    /**
     * Modifies the phone or address attributes of an account (Op5).
     * Every role can use this method, but with some exceptions:
     * USER (self only), BOFFICER (self or USERs), ADMIN (anyone).
     * @param input Contains the username of the user we want to modify, the attributes (phone or address) and the Token.
     * @return 200 OK with modifying the user attributes, or appropriate error (e.g., 9901, 9906).
     */
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

        // Enforce the exceptions described above
        if (modifyingRole.equals(Role.USER.toString()) && !modifyingUsername.equals(targetedUsername))
            return errorResponse(ErrorCodes.UNAUTHORIZED);

        else if (modifyingRole.equals(Role.BOFFICER.toString()) && !modifyingUsername.equals(targetedUsername)
                && !targetedRole.equals(Role.USER.toString()))
            return errorResponse(ErrorCodes.UNAUTHORIZED);


        try {
            // Entity.Builder allows us to modify the Property of an entity's table
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

    /**
     * Shows the role of a specific user (Op7).
     * The only roles that can perform this method are ADMIN and BOFFICER.
     * @param input Contains the username of the user we want to see the Role and the Token.
     * @return 200 OK showing the Role the username passed in the input is, or appropriate error (e.g., 9901, 9906).
     */
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


    /**
     * Changes the role of a specific user (Op8).
     * The only role that can perform this method is ADMIN.
     * Admins cannot change their own roles.
     * @param input Contains the username of the user we want to change Role, the new Role of the user and the Token.
     * @return 200 OK with changing the users Role, or appropriate error (e.g., 9901, 9906).
     */
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

        // Prevent Admins from modifying their own role
        String tokenUsername =  token.getString("user_name");
        if (input.getInput().getUsername().equals(tokenUsername))
            return errorResponse(ErrorCodes.FORBIDDEN);

        try {
            // Update the role in the User table
            Entity.Builder updatedUser = Entity.newBuilder(targetUser);
            updatedUser.set("user_role", newRole);

            datastore.put(updatedUser.build());

            // Update the role in all session tokens for this user
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

    /**
     * Changes a user's password (Op9).
     * All roles are allowed to use this method, but users can ONLY change their own password.
     * @param input Contains the username of the user we want to change the pwd, the old and new pwd, and the Token.
     * @return 200 OK with modifying the user pwd, or appropriate error (e.g., 9901, 9906).
     */
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

        // A user can only change their own password
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
            // Update the new password in the User table
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
