package com.example.DM_Final;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("eer")
public class Controller {

    @POST()
    @Path("/dynamic")
    @Produces("application/json")
    @Consumes("text/plain")
    public Object dynamic(String request) throws Exception {
        System.out.println("Handle operation ");
        DynamicMappingParser parser = new DynamicMappingParser();
        Object parse = parser.parse(request);
        if(parse.getClass().getSimpleName().equals("String"))
            return parse;
        return new CloneObject().clone(parse);
    }
}

