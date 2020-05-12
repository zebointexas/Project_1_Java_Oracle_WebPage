 
/*
 * Created:   04/06/2002
 * Modified:  04/07/2002
 *            02/16/2003
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

public class insertNewEmployers extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

	ematch inst = new ematch(1);  // 1: means insert, not a query operation

        try {
	    String insertStmt = inst.buildInsertEmployerStmt(request);

	    inst.printHeader(out);

            out.println("<pre>");
	    out.println("Insert STMT: " + insertStmt);

	    if (insertStmt.substring(0,5).equals("Error"))
	    	out.println("Insert operation not performed due to errors. " + insertStmt);
	    else if (insertStmt.substring(0,6).equals("INSERT"))
		inst.doInsert(insertStmt);
	
	    inst.printTailer(out);
 	}
	catch (SQLException e)
	{
   	    out.println("SQL Exception: "+e.getMessage());
	}
    }
}
