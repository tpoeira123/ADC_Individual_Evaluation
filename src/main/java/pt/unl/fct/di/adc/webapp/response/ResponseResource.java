package pt.unl.fct.di.adc.webapp.response;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.gson.Gson;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Abstract class for all REST endpoints.
 * Standardizes JSON response building
 * to keep individual endpoint logic clean (don't repeat yourself).
 */
public abstract class ResponseResource {

    protected static Gson gson = new Gson();

    /**
     * Packages a predefined ErrorCode into a 200 OK HTTP Response.
     */
    protected Response errorResponse(ErrorCodes error) {
        String codeError = String.valueOf(error.getErrorCode());
        String description = error.getDescription();
        
        ApiResponse response = new ApiResponse(codeError,  description);

        return Response.ok(gson.toJson(response)).build();
    }

    /**
     * Packages a successful data map into a 200 OK HTTP Response.
     */
    protected Response successResponse(Map<String, Object> data) {
        ApiResponse response = new ApiResponse("success", data);

        return Response.ok(gson.toJson(response)).build();
    }

    /**
     * Helper method to quickly generate a standard {"message": "..."} data map.
     */
    protected Map<String, Object> messageData(String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", message);

        return data;
    }

}
