/*
 * Created:   04/01/2002
 * Modified:  04/03/2002
 *            02/14/2003
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

public class getJob extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();


        ematch s1 =  new ematch(0);

        try {
	    //String bodyTag = "<body bgcolor=#ffffff topmargin=5 marginheight=5 leftmargin=0 marginwidth=0>";
	    String bodyTag = "<body background=\"/AIIPDemos/images/f_bkg.gif\"" +
				"text=\"#000000\" link=\"8FF0FF0\" vlink=\"#551A8B\" alink=\"#FF0000\">";
	    s1.printHeaderWithBodyTag(out, bodyTag);

	    s1.getAndDisplayAJobDetails (request, response, out);

	    //out.println("Still have to implement the method doCandidateQuery");

	    s1.printTailerWithoutReference(out);
	    out.close();
  	}
	catch (SQLException e)
	{
   	    out.println("SQL Exception: "+e.getMessage());
	}
    }
}
