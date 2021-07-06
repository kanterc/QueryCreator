package hr.cs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/cs") //cs = candidate search
public class SearchResource {
    @GET
    @Path("/{request}")
    @Produces("application/json")
    public String printHello(@PathParam("request") String request)
    {
        String[] terms = request.split("&");
        Search s = new Search();
        String searchInput = null;
        String show = terms[1].substring(5);
        String location = null;
        String sourcedBy = null;
        String query = "";

        if(!terms[0].substring(6).equals(""))
        {
            searchInput = terms[0].substring(6);
        }
        if(!terms[3].substring(9).equals(""))
        {
            location = terms[3].substring(9);
        }
        if(show.equals("Yours"))
        {
            sourcedBy = terms[4].substring(9);
        }
        query = s.createSearchQuery(searchInput, location, show, sourcedBy);
        return query;
    }
}
