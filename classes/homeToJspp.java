 
import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;
import java.io.*;

public class homeToJspp extends HttpServlet {

    public void doGet (HttpServletRequest request,
		       HttpServletResponse response) {

	try {
	    // Set the attribute and Forward to hello.jsp
	    request.setAttribute ("servletName", "homeToJspp");
	    ematch s = new ematch(0);

	    //PrintWriter out = response.getWriter();
	
	    //s.printHeader(out);
	    //out.println("<pre>");
	    //out.println("This is a test");
	    
	    //out.println("</pre>");
	    //out.println("<p>");
	//out.println("<jsp:include page=\"/jsp/include/employerLogin.html\" flush=\"true\"/>");
	//out.println("<jsp:include page=\"/jsp/include/foo.html\" flush=\"true\"/>");
	//out.println("<jsp:include page=\"/jsp/include/include.jsp\" flush=\"true\"/>");
	 request.setAttribute ("servletName", "homeToJspp");
	 getServletConfig().getServletContext().getRequestDispatcher("/jsp/include/test.jsp").include(request, response);

	    //out.printTailer(out);

	} catch (Exception ex) {
	    ex.printStackTrace ();
	}
    }
}
