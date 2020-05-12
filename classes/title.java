 
// Get the current job and member amount
/*
 * Created:   04/08/2002
 * Modified:  04/09/2002
 *            02/15/2003
 *            04/06/2006
 */

import java.sql.*; 
import java.math.*;
import java.io.*;
import java.awt.*;

import oracle.jdbc.driver.*;

import javax.servlet.*;
import javax.servlet.http.*;


public class title extends HttpServlet {

   public void doGet (HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException
   {
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();

	  out.println("<HTML>");
	  out.println("<HEAD>");
	  out.println("<TITLE>Bobcat Spirit Job Search</TITLE>");
	  out.println("</HEAD>");

	  out.println("<BODY bgcolor=\"#000066\" LINK=\"#000000\" >");
	 
	  out.println("<div style=\"display:inline-block;vertical-align:top;\" >");
			out.println("<a href=\"https://www.linkedin.com/jobs/ \" target=\"_blank\" >");
			out.println("<img src=\"http://newfirebird.cs.txstate.edu:8080/z_x3/images/111.jpg\" alt=\"Italian Trulli\"   style=\"width:200px;height:120px;\" >");
			out.println("</a>");
	  out.println("</div>");
	  
	  out.println("<div style=\"display:inline-block;\" >");

			 
			out.println("<br>");
			out.println("<div><H3> <i><font color=\"#ffcc00\">Click the Bobcat to visit external jobs</font></i></H3></div>");
		 
 
	  out.println("</div>"); 
			   
	   
	  out.println("<form name=advancedmanpower_title>");
	  out.println("<table width=\"800\">");
	//  out.println("<tr>");
	//  out.println("<td>");
	//  out.println("<H1> <i><font color=\"#ffcc00\"> https://www.linkedin.com/jobs/ </font></i></H1>");
	//  out.println("</td>");
	//  out.println("<td>&nbsp;</td>");
	//  out.println("</tr>");
	  
	 
	  out.println("<br>");
	  out.println("<font color=\"#ffffcc\">The online job search portal deficated for 2020 CS graduates all over the world!<br>&nbsp");
	  
	 
	 
  
	  out.println("<tr>");
	  out.println("<td colspan = 1>");
	  out.println("<center>");
	  out.println("<TABLE CELLSPACING=\"0\" CELLPADDING=\"0\" width=\"60%\" bgcolor=\"##000066\">");
	  out.println("<tr>");
	  out.println("	<td> <input type=button value=\"Home\" onClick=\"top.main.location='http://newfirebird.cs.txstate.edu:8080/z_x3/jsp/home.jsp'\"></td>");
	  out.println("	<td> <input type=button value=\"Job Search\" onClick=\"top.main.location='http://newfirebird.cs.txstate.edu:8080/z_x3/servlets/jobSearch.html'\"></td>");
	  out.println("	<td> <input type=button value=\"Employers\" onClick=\"top.main.location='http://newfirebird.cs.txstate.edu:8080/z_x3/servlets/employerLogin.html'\"></td>");
	  out.println("	<td> <input type=button value=\"Members\" onClick=\"top.main.location='http://newfirebird.cs.txstate.edu:8080/z_x3/servlets/candidateLogin.html'\"></td>");
	  out.println("</tr>");
	  out.println("</table>");
	  out.println("</center>");
	  out.println("</td>");
	  out.println("</tr>");
	  out.println("</table>");
	  out.println("</form>");


	  out.println("</BODY>");
	  out.println("</html>");
   }
}
