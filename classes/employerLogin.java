/*
 * Created:   02/22/2005
 */
 
import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*; 
import java.math.*;
import java.awt.*;

import oracle.jdbc.pool.OracleDataSource; // Oracle 10

/**
 * The servlet that performs verification of a member.
 *
 */

public class employerLogin extends HttpServlet {

    public ematchConstStruct constStruct = new ematchConstStruct();

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
	try {
		OracleDataSource ods = new OracleDataSource();
		ods.setURL("jdbc:oracle:oci8:" + constStruct.USERNAME + "/" + constStruct.PASSWORD + "@" + constStruct.DATABASE);
		Connection conn = ods.getConnection();
		//Connection conn = DriverManager.getConnection
		//     ("jdbc:oracle:oci:@gla92010", constStruct.USERNAME, constStruct.PASSWORD);
	    
		// Query the employee names 
		Statement stmt = conn.createStatement (); 
		ResultSet rset = stmt.executeQuery ("SELECT emp_id, email FROM employer");

		response.setContentType("text/html");

		ematch em = new ematch(1);
		String empId, id, loginEmail, email;

		empId = String.valueOf(request.getParameter("empId"));
		loginEmail = String.valueOf(request.getParameter("password"));

		int matched = 0;
		while (rset.next ()) {

			 id = new String(rset.getString(1));
			 email = new String (rset.getString (2));

			 if (empId.equals(id) && loginEmail.equals (email)) {
				 //out.println("A successful match!");
				 matched = 1;
				 break;
			 }
		}

		if (matched == 0) {
			PrintWriter out = response.getWriter();
			em.printHeader(out);
			out.println("Given emp_id=" + empId +
						"-- and email=" + loginEmail + "--");
			out.println("<pre>");
			out.println("Sorry, wrong emp_id or password (email address), try again!");
			out.println("</pre>");
			em.printTailer(out);
			stmt.close();
			out.close();
			conn.close();
		} else {
		        stmt.close();
			//request.setAttribute("selectedScreen", request.getServletPath());
			//RequestDispatcher dispatcher = request.getRequestDispatcher("homeToJsp.jsp");	
			RequestDispatcher dispatcher = request.getRequestDispatcher("/servlets/submitNewJob.html");	
			//RequestDispatcher dispatcher = request.getRequestDispatcher("/servlets/jobSearch.html");	
			if (dispatcher != null)	
				dispatcher.forward(request, response);	
		}
	}
	catch (SQLException e)
	{
   		System.out.println("SQL Exception: "+e.getMessage());
	}
    }
}
