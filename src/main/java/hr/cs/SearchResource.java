package hr.cs;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/cs") //cs = candidate search
public class SearchResource {
    @GET
    @Produces("application/json")
    public Response searchCandidate(@DefaultValue("") @QueryParam("terms") String input, @DefaultValue("") @QueryParam("status") String s, @DefaultValue("") @QueryParam("show") String s2,
                                    @QueryParam("page") int page, @DefaultValue("") @QueryParam("location") String loc,
                                    @DefaultValue("") @QueryParam("sourceby") String uid)
    {
        Search search = new Search();
        String searchInput = null;
        String show = s;
        String location = null;
        String sourcedBy = null;
        String query = "";
        if(!input.equals("")) {
            searchInput = input;
        }
        if(!loc.equals("")) {
            location = loc;
        }
        if(show.equals("")) {
            show = s2;
        }
        if(!uid.equals("")) {
            sourcedBy = uid;
        }
        query = search.createSearchQuery(searchInput, location, show, sourcedBy);
        return Response.status(200).entity(query).build();
    }
}
