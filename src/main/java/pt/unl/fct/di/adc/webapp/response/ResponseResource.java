package pt.unl.fct.di.adc.webapp.response;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.gson.Gson;
import com.google.protobuf.Api;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.adc.webapp.enums.ErrorCodes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ResponseResource {

    protected static Gson gson = new Gson();
    protected static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    protected Response errorResponse(ErrorCodes error) {
        String codeError = String.valueOf(error.getErrorCode());
        String description = error.getDescription();
        
        ApiResponse response = new ApiResponse(codeError,  description);

        return Response.ok(gson.toJson(response)).build();
    }

    protected Response successResponse(Map<String, Object> data) {
        ApiResponse response = new ApiResponse("success", data);

        return Response.ok(gson.toJson(response)).build();
    }

    protected Map<String, Object> messageData(String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("message", message);

        return data;
    }

}
