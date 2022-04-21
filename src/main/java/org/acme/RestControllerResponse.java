package org.acme;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/api")
public class RestControllerResponse {

    @Path("/generate")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RequestBody(
            content = @Content(
                    examples = {
                            @ExampleObject(name = "Example1", ref = "Example1"),
                            @ExampleObject(name = "Example2", ref = "Example1")
                    }))
    public String generator(final Map<String, Object> input) throws Exception {
        return "Hello From Generator Method";
    }
}
