// Get the current job and member amount
/*
 * Created:   04/07/2002
 * Modified:  04/09/2002
 *            02/15/2003
 *
 *            02/19/2005
 *            02/21/2005
 *            04/06/2006
 */

 
 
import java.sql.*; 
import java.math.*;
import java.io.*;
import java.awt.*;

import oracle.jdbc.driver.*;

import javax.servlet.*;
import javax.servlet.http.*;

import oracle.jdbc.pool.OracleDataSource;

public class home extends HttpServlet {

   public static int memberNumber;
   public static int jobNumber;
   public static int collGradNumber;
   public ematchConstStruct constStruct = new ematchConstStruct();

   public void doGet (HttpServletRequest request,
                      HttpServletResponse response) throws ServletException, IOException
   {
      // Load Oracle driver
      try {
      //DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

      // Connect to the local database
      //Connection conn = DriverManager.getConnection
 	//		("jdbc:oracle:oci:@gla92010", "peng", "Sm97ireT");

        OracleDataSource ods = new OracleDataSource();
        ods.setURL("jdbc:oracle:oci8:" + constStruct.USERNAME + "/" + constStruct.PASSWORD + "@" + constStruct.DATABASE);
        Connection conn = ods.getConnection();

     // ResourceBundle rb =
     //      ResourceBundle.getBundle("LocalStrings",request.getLocale());
      
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();

      // Query the member table 
      Statement stmt = conn.createStatement (); 
      ResultSet rset = stmt.executeQuery ("SELECT count(*) FROM member");
      rset.next();
      memberNumber = rset.getInt(1);
      rset.close();

      // Query the job table 
      //Statement stmt = conn.createStatement (); 
      rset = stmt.executeQuery ("SELECT count(*) FROM job");
      rset.next();
      jobNumber = rset.getInt(1);
      rset.close();

      // Query the c_g table 
      //Statement stmt = conn.createStatement (); 
      rset = stmt.executeQuery ("SELECT count(*) FROM C_G");
      rset.next();
      collGradNumber = rset.getInt(1);

      rset.close();
      stmt.close();
      conn.close();

      out.println(" <HTML>");
      out.println(" <HEAD>");
      out.println(" <TITLE>Bobcat Spirit Job Search</TITLE>");
      out.println(" </HEAD>");

      out.println(" <BODY BGCOLOR=\"#FFFFFF\" LINK=\"#0088ff\" ALINK=\"#FF0000\" VLINK=\"#CC0000\">");

      out.println(" <center><h2>Bobcat Job Search</h2></center>");
      out.println(" <center>");

      out.println(" <IMG src=\"/z_x3/images/background.gif\" width=\"550\"" +
		      "height=\"180\"  usemap=\"#map1\" border=\"0\"><map name=map1>");
      out.println(" <area shape=\"rect\" coords=\"3,3,215,180\"" +
		      	"href=\"http://newfirebird.cs.txstate.edu:8080/z_x3/servlets/jobSearch.html\">");
      out.println(" <area shape=\"rect\" coords=\"310,3, 550,180\"" +
		      "href=\"http://newfirebird.cs.txstate.edu:8080/z_x3/servlets/employerLogin.html\">");
      out.println(" </map>");

      out.println(" <br>");
      out.println(" <br>");
      out.println(" <center>");
      out.println(" <i>");
      out.println(" Registered Engineers: " + memberNumber + "<br>");
      out.println(" Total jobs: " + jobNumber + "<br>");
      out.println(" Total registered college graduates: " + collGradNumber + "<br>");
      out.println(" </i>");
      out.println(" </center>");
      out.println(" <br>");
      out.println(" <br>");
      out.println(" <b>");
      out.println(" DrPengspeng.com will assist and challenge you to be where you can be in the 21th centry in the high tech, high competitive, high reward world.");
      out.println(" </b>");
      out.println(" <br>");
      out.println(" <br>");
      out.println(" <br>");
      out.println(" <TABLE CELLSPACING=\"0\" CELLPADDING=\"3\" BORDER=\"0\">");
      out.println(" <tr>");
      out.println(" <td> <A href=\"http://newfirebird.cs.txstate.edu:8080/z_x3/jsp/home.jsp\"> Home </A>");
      out.println(" <td> <A href=\"http://newfirebird.cs.txstate.edu:8080/z_x3/servlets/jobSearch.html\">Job Search </a>");
      out.println(" <td> <A href=\"http://newfirebird.cs.txstate.edu:8080/z_x3/servlets/employerLogin.html\">Employers </A>");
      out.println(" <td> <A href=\"http://newfirebird.cs.txstate.edu:8080/z_x3/servlets/candidateLogin.html\">Members </A>");
      out.println(" </tr>");
      out.println(" </table>");
      out.println(" </center>");
      out.println(" <br>");
      out.println(" <i>Copyright @2020 AggieSpiritMentorChain.com Inc. All rights reserved.");
      out.println(" </body>");
      out.println(" </html>");
      }
      
      catch (SQLException e)
      {
   	    System.out.println("SQL Exception: "+e.getMessage());
      }
   }
  
}
 

