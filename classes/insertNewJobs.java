 
/*
 * Created:   03/30/2002
 * Modified:  04/01/2002
 *            04/02/2002
 *            02/18/2003
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

public class insertNewJobs extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

	ematch inst = new ematch(1);

        try {
	    String insertStmt = inst.buildInsertJobStmt(request);

	    inst.printHeader(out);

            out.println("<pre>");
	    out.println("Insert STMT: " + insertStmt);

	    if (insertStmt.substring(0,6).equals("INSERT"))
		    inst.doInsert(insertStmt);
	
	    inst.printTailer(out);
 	}
	catch (SQLException e)
	{
   	    out.println("SQL Exception: "+e.getMessage());
	}
    }
}
