package hr.cs;

public class Search {
    /**
     * Variables
     */
    private InputParser parser = new InputParser();

    /**
     * Constructor
     */
    public Search()
    {

    }

    /**
     * Creates the complete query to be used for searching candidates based on boolean search input from Hyrerocket.
     */
    public String createSearchQuery(String terms, String location, String status, String sourcedBy)
    {
        String query = "";
        parser.loadInput(terms, location, status, sourcedBy);
        parser.parseTermsInput();
        if(!parser.getQueryWhereClause().equals("Error in the search input")) {
            parser.addLocationToQuery();
            parser.addStatusToQuery();
            parser.addSourcedByToQuery();
            parser.addOrderByToQuery();
            parser.createSelectQuery();
            query = parser.getSelectQuery().concat(parser.getQueryWhereClause());
            query = query.concat(";");
        }
        else {
            query = parser.getQueryWhereClause();
        }
        parser.resetVariables();
        return query;
    }
}
