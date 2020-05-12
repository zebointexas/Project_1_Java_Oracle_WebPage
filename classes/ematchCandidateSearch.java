/*
 * Created:   04/01/2002
 * Modified:  04/03/2002
 *            02/15/2003
 */

 

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*; 
import java.math.*;
import java.awt.*;


/**
 * The servlet that performs the job search action.
 *
 */

public class ematchCandidateSearch extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();


        ematch s1 =  new ematch(0);

        try {
	    s1.printHeader(out);
            out.println("<pre>");

	    String sqlstmt = s1.buildCandidateQuery(request, 3); // 3: MEMBER_SEARCH

	    if (sqlstmt.equals("-1") ) {
		out.println("Invalid selection of search fields. Please try again!");
                out.println("</pre>");
	        s1.printTailer(out);
	        out.close();
		return;
	    }

	    out.println("SQL STMT: " + sqlstmt);
            out.println("</pre>");

	    s1.doCandidateQuery(sqlstmt, out);

	    //out.println("Still have to implement the method doCandidateQuery");

	    s1.printCandidateQueryResultBody(out);

	    s1.printTailer(out);
	    out.close();
  	}
	catch (SQLException e)
	{
   	    out.println("SQL Exception: "+e.getMessage());
	}
    }
}
