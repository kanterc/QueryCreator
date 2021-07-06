package hr.cs;

import java.util.ArrayList;
import java.util.Locale;

public class InputParser {
    /**
     * Variables
     */
    private String input;
    private String location;
    private String status;
    private String sourcedBy;
    private String selectQuery;
    private String whereClause;
    private int selectType = 1; //Indicates if the select type will contain ranking or just a general search 1=Ranking, 0=General
    private ArrayList<String> keywords = new ArrayList<>();

    /**
     * Constructors
     */
    public InputParser() {}
    public InputParser(String input)
    {
        this.input = input;
    }

    /**
     * Load Input Method
     * Loads a new boolean search input into the input variable
     */
    public void loadInput(String input, String location, String status, String sourcedBy)
    {
        this.input = input;
        if(this.input != null && this.input.charAt(this.input.length()-1) == ' ')
        {
            this.input = input.substring(0, input.length()-1);
        }
        if(this.input != null) {
            this.input = this.input.toLowerCase(Locale.ROOT);
        }
        if(location != null) {
            this.location = location.toLowerCase(Locale.ROOT);
        }
        this.status = status.toLowerCase(Locale.ROOT);
        this.sourcedBy = sourcedBy;
    }

    /**
     * Get Input Method
     * Returns the current input being used.
     */
    public String getInput()
    {
        return this.input;
    }

    /**
     * Get Query Where Clause Method
     * Returns the where clause that was constructed based on the current input.
     */
    public String getQueryWhereClause()
    {
        return this.whereClause;
    }

    /**
     * Reset Where Clause variable Method
     * Resets the value of the whereClause variable to empty
     */
    public void resetWhereClauseValue()
    {
        this.whereClause = "";
    }

    /**
     * Get Keywords Method
     * Returns the list of keywords found in the input string
     */
    public ArrayList<String> getKeywords()
    {
        return keywords;
    }

    /**
     * Reset keywords list Method
     * Empties the keywords list so it is ready for a new set of keywords.
     */
    public void resetVariables() {
        keywords.clear();
        input = "";
        location = "";
        status = "";
        sourcedBy = "";
        selectQuery = "";
        whereClause = "";
        selectType = 1;
    }


    /**
     * Get select query Method
     * Return the select query created.
     */
    public String getSelectQuery()
    {
        return this.selectQuery;
    }

    /**
     * Create select and joins Method
     */
    public void createSelectQuery()
    {
        if(selectType == 0 || keywords.size() == 0) {
            selectQuery = "select distinct(c.id)\n" +
                    "from candidates c\n" +
                    "left outer join locations l\n" +
                    "    on c.id = l.locationable_id\n" +
                    "left outer join countries co\n" +
                    "    on l.country_id = co.id\n" +
                    "left outer join states s\n" +
                    "    on l.state_id = s.id\n" +
                    "left outer join cities cc\n" +
                    "    on l.city_id = cc.id \n";
        }
        else {
            selectQuery = "select distinct(c.id), \n" +
                    "(";
            for(int i=0; i<keywords.size(); i++)
            {
                selectQuery = selectQuery.concat("(ROUND (\n" +
                        "        (\n" +
                        "            LENGTH(lower(resume_content))\n" +
                        "            - LENGTH( REPLACE ( lower(resume_content), '"+keywords.get(i)+"', '') )\n" +
                        "        ) / LENGTH('"+keywords.get(i)+"')\n" +
                        "    ))");
                if(i<keywords.size()-1){ //Its not the last keyword so we add the + sign
                    selectQuery = selectQuery.concat(" + ");
                }
            }
            selectQuery = selectQuery.concat(") AS rankSum \n" +
                    "from candidates c\n" +
                    "left outer join locations l\n" +
                    "    on c.id = l.locationable_id\n" +
                    "left outer join countries co\n" +
                    "    on l.country_id = co.id\n" +
                    "left outer join states s\n" +
                    "    on l.state_id = s.id\n" +
                    "left outer join cities cc\n" +
                    "    on l.city_id = cc.id \n");
        }
    }

    /**
     * Create name where clause Method
     * Creates the name where clause
     */
    public String createNameWhereClause(String input)
    {
        String queryClause = "lower(concat(c.first_name,' ',c.last_name)) like '";
        String[] words = input.split(" ");
        for(int i=0; i<words.length; i++)
        {
            queryClause = queryClause.concat("%"+words[i]);
        }
        queryClause = queryClause.concat("%'\n");
        return queryClause;
    }

    /**
     * Parse Input Method
     * Receives a string with the boolean search input text and creates a where clause query based on the input.
     */
    public void parseTermsInput()
    {
        String nextWord = "";
        whereClause = "where ";
        int closingParenthesis = 0;
        boolean nameSearch = false;
        boolean emailSearch = false;
        boolean andOr = false;

        //Before starting we check if we are looking for a name or an email
        if(input == null) {
            System.out.println("Entro aqui");
            selectType = 0; //It did not have terms which means it is a general search
        }
        else if(this.input.contains("@"))
        {
            whereClause = whereClause.concat("c.email like '%"+this.input+"%'");
            emailSearch = true;
        }
        else if(this.input.contains("name"))
        {
            String name2search = this.input.substring(4, this.input.length()-1);
            if(name2search.charAt(0) == ' ')
            {
                name2search = name2search.substring(1, name2search.length()-1);
            }
            whereClause = whereClause.concat(createNameWhereClause(name2search.toLowerCase()));
            nameSearch = true;
        }
        else
        {
            //whereClause = whereClause.concat("(lower(concat(c.first_name,' ',c.last_name)) like '%"+this.input+"%'\n" +
            //        "    or (");
            whereClause = whereClause.concat("("+createNameWhereClause(this.input));
            whereClause = whereClause.concat("  or (");
            for(int i = 0; i < this.input.length(); i++)
            {
                char character = input.charAt(i);
                if (character == '(')
                {
                    whereClause = whereClause + character;
                    closingParenthesis++; //Every time a opening parenthesis is found it adds one unit to the variables
                }
                else if (character == ')' && nextWord.equals(""))
                {
                    whereClause = whereClause + character;
                    closingParenthesis--; // Every time a closing parenthesis is found it subtracts one unit from the variable
                }
                else if (character == ')' && !nextWord.equals(""))
                {
                    whereClause = whereClause.concat("lower(c.resume_content) like '%" + nextWord.toLowerCase() + "%')");
                    keywords.add(nextWord);
                    nextWord = "";
                    closingParenthesis--;
                }
                else if (character == '"')
                {
                    try
                    {
                        int endFlag = i;
                        do
                        {
                            endFlag++;
                            character = this.input.charAt(endFlag);
                        }
                        while (character != '"');
                        nextWord = nextWord.concat(this.input.substring(i + 1, endFlag));
                        i = endFlag;
                        if (i == this.input.length() - 1)
                        {
                            whereClause = whereClause.concat("lower(c.resume_content) like '%" + nextWord.toLowerCase() + "%'");
                            keywords.add(nextWord);
                            nextWord = "";
                        }
                    }
                    catch (Exception e)
                    {
                        //The user did not close correctly the compounded phrase with quotation marks
                        whereClause = "Error in the search input";
                        andOr = false;
                        break;
                    }
                }
                else if (character == ' ' && !nextWord.equals(""))
                {
                    if (nextWord.equals("or"))
                    {
                        whereClause = whereClause.concat("\nor ");
                        andOr = true;
                    }
                    else if (nextWord.equals("and"))
                    {
                        if (i == this.input.length() - 1)
                        {
                            //The user inputted a compound phrase with two or more words without using quotation marks This would be considered an error
                            //This can also be the case that the user is searching for a users name, so the query will be changed to a name search only
                            //whereClause = "Error in the search input";
                            //whereClause = "where lower(concat(c.first_name,' ',c.last_name)) like '%"+ this.input.toLowerCase() +"%'";
                            whereClause = whereClause.concat("where ");
                            whereClause = whereClause.concat(createNameWhereClause(this.input.toLowerCase()));
                            nameSearch = true;
                            andOr = false;
                            break;
                        }
                        else if (this.input.substring(i + 1, i + 4).equals("not"))
                        {
                            whereClause = whereClause.concat("\nand not ");
                            i = i + 4;
                        }
                        else
                        {
                            whereClause = whereClause.concat("\nand ");
                            System.out.println("Entro al else del and que puso un and en el query "+i+" "+nextWord);
                            andOr = true;
                        }
                    }
                    else
                    {
                        if (i + 4 < this.input.length() - 1 && !this.input.substring(i + 1, i + 5).equals("and ") && !this.input.substring(i + 1, i + 4).equals("or ")) {
                            //The user inputted a compound phrase with two or more words without using quotation marks This would be considered an error
                            //whereClause = "Error in the search input";
                            //whereClause = ("where lower(concat(c.first_name,' ',c.last_name)) like '%"+ this.input.toLowerCase() +"%'");
                            whereClause = "where ";
                            whereClause = whereClause.concat(createNameWhereClause(this.input.toLowerCase()));
                            nameSearch = true;
                            andOr = false;
                            System.out.println("Entro al and sin nada despues");
                            break;
                        }
                        else if (this.input.length() - 1 < i + 4) {
                            //The user inputted a compound phrase with two or more words without using quotation marks This would be considered an error
                            //whereClause = "Error in the search input";
                            //whereClause = ("where lower(concat(c.first_name,' ',c.last_name)) like '%"+ this.input.toLowerCase() +"%'");
                            whereClause = whereClause.concat("where ");
                            whereClause = whereClause.concat(createNameWhereClause(this.input.toLowerCase()));
                            nameSearch = true;
                            andOr = false;
                            System.out.println("Entro al an o");
                            break;
                        }
                        else if(andOr == true)
                        {
                            //It found an and or an or and there is a word after it so it will be added to the query
                            whereClause = whereClause.concat("lower(c.resume_content) like '%" + nextWord.toLowerCase() + "%'");
                            keywords.add(nextWord);
                            andOr = false;
                            System.out.println("Entro al else if");
                        }
                        else
                        {
                            //It found a word before a space so it will be added to the query as a single word search
                            whereClause = whereClause.concat("lower(c.resume_content) like '%" + nextWord.toLowerCase() + "%'");
                            keywords.add(nextWord);
                            andOr = false;
                            System.out.println("Entro al else final de palabra sola valida");
                        }
                    }
                    nextWord = "";
                }
                else if(character == ' ' && nextWord.equals(""))
                {
                    //Do Nothing, it is a blank space at the start of a line which will not be contemplated.
                }
                else if(i == this.input.length() - 1) //We are at the end of the input and will add the last word being obtained
                {
                    if(andOr == true) { //There was an and/or added in the search string
                        nextWord = nextWord.concat(String.valueOf(character));
                        whereClause = whereClause.concat("lower(c.resume_content) like '%" + nextWord.toLowerCase() + "%'");
                        keywords.add(nextWord);
                        andOr = false;
                        System.out.println("Entro al if del andOr siendo true y que ya esta al final del input");
                    }
                    else
                    {
                        //The user tried to input a compounded word without using quotation marks, it will be considered it is looking for a name or a single word search
                        whereClause = ("where (" + createNameWhereClause(this.input) +
                                "  or lower(c.resume_content) like '%" + this.input + "%')");
                        nameSearch = true;
                        andOr = false;
                        System.out.println("Entro al else final donde el user o la cago o anda buscando un name");
                    }
                }
                else
                {
                    nextWord = nextWord + character;
                }
            }
            if(!whereClause.equals("Error in the search input") && nameSearch == false && emailSearch == false)
            {
                if (closingParenthesis != 0) // If by the end of the input string there is not a balanced number of parenthesis an error will be returned
                {
                    whereClause = "Error in the search input";
                }
                else
                {
                    //It was a normal boolean search so we should end it up with the two parenthesis
                    whereClause = whereClause.concat("))");
                }
            }
        }
        System.out.println(whereClause);
    }

    /**
     * Add Location Method
     * Add the location where statement to the pre created query
     */
    public void addLocationToQuery()
    {
        if(this.location != null && !this.location.equals(""))
        {
            if(whereClause.equals("where ")){
                whereClause = whereClause.concat("l.locationable_type = 'App\\\\Models\\\\Candidate'\n" +
                        " and (lower(co.name) like '" + location + "'\n" +
                        "    or lower(s.name) like '" + location + "'\n" +
                        "    or lower(cc.name) like '" + location + "')");
            }
            else{
                whereClause = whereClause.concat("\nand l.locationable_type = 'App\\\\Models\\\\Candidate'\n" +
                        " and (lower(co.name) like '" + location + "'\n" +
                        "    or lower(s.name) like '" + location + "'\n" +
                        "    or lower(cc.name) like '" + location + "')");
            }
        }
    }

    /**
     * Add candidate status Method
     * Adds the status of the candidate to the existing where clause pre created query
     */
    public void addStatusToQuery()
    {
        String and = "";
        if(!whereClause.equals("where ")) {
            and = "\nand ";
        }
        if(status != null)
        {
            if(this.status.equals("pasive")) {
                whereClause = whereClause.concat(and+"c.candidate_status_id = 1");
            }
            else if(status.equals("active")) {
                whereClause = whereClause.concat(and+"c.candidate_status_id = 2");
            }
            else if(status.equals("hired")) {
                whereClause = whereClause.concat(and+"c.candidate_status_id = 3");
            }
            else if(status.equals("do not hire")) {
                whereClause = whereClause.concat(and+"c.candidate_status_id = 4");
            }
            else if(status.equals("all") && selectType == 0) { //We have a general search and we should just reset the where clause to not have anything from status
                whereClause = "";
            }
        }
    }

    /**
     * Add sourced by id Method
     * Adds the sourced by id (If existing) of the user requesting his candidates to the pre created query
     */
    public void addSourcedByToQuery()
    {
        if(this.sourcedBy != null && !this.sourcedBy.equals("") && this.status.equals("yours"))
        {
            if(whereClause.equals("where ")){
                whereClause = whereClause.concat("c.source_by = " + this.sourcedBy);
            }
            else {
                whereClause = whereClause.concat("\nand c.source_by = " + this.sourcedBy);
            }
        }
    }

    /**
     * Add order by to query Method
     * Adds the order by clause to the end of the query so it gets ordered by the rankSum calculated column
     */
    public void addOrderByToQuery()
    {
        if(selectType == 1 && keywords.size() > 0) {
            if (!whereClause.equals("")) {
                whereClause = whereClause.concat("\norder by rankSum desc");
            } else {
                whereClause = whereClause.concat("order by rankSum desc");
            }
        }
    }
}
