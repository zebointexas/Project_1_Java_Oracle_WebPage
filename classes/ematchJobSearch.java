/*
 * Created:   02/10/2002
 * Modified:  03/10/2002
 *            03/28/2002
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

public class ematchJobSearch extends HttpServlet {

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

	    String sqlstmt = s1.buildJobQuery(request, 1); // 1: JOB_SEARCH

	    if (sqlstmt.equals("-1") ) {
		out.println("Invalid selection of search fields. Please try again!");
                out.println("</pre>");
	        s1.printTailer(out);
	        out.close();
		return;
	    }

	    out.println("SQL STMT: (Built by Zebo) " + sqlstmt);
            out.println("</pre>");

	    s1.doJobQuery(sqlstmt,out);



	    s1.printJobQueryResultBody(out);


	    s1.printTailer(out);
	    out.close();
  	}
	catch (SQLException e)
	{
   	    out.println("SQL Exception from Zebo: "+e.getMessage());
	}
    }
}
