 
import java.sql.*; 
import java.math.*;
import java.io.*;
import java.awt.*;
import oracle.jdbc.driver.*;


class JdbcTest1 {

   private static String QueryRows[];
   public static int i;

   public String [] doQuery (String sqlstmt) throws SQLException, IOException { 
      // Load Oracle driver
      DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

      //Class.forName ("oracle.jdbc.driver.OracleDriver");
      // Connect to the local database
      //Connection conn = new oracle.jdbc.driver.OracleDriver().defaultConnection(); 
      //Connection conn = DriverManager.getConnection 
      //	("jdbc:oracle:thin:@ORCL817","scott", "tiger");
      //Connection conn = DriverManager.getConnection 
      //	("jdbc:oracle:kprb:@ORCL817","scott", "tiger");
      //          ("jdbc:oracle:kprb:@localhost:1521:ORCL817","scott", "tiger"); 
      //Connection conn =  
      //     new oracle.jdbc.driver.OracleDriver().defaultConnection();

      Connection conn = DriverManager.getConnection
 			("jdbc:oracle:oci:@gla101", "scott", "tiger");
    
      // Query the employee names 
      Statement stmt = conn.createStatement (); 
      ResultSet rset = stmt.executeQuery (sqlstmt);

      i = 0;
      // Print the name out 
	  QueryRows = new String [100];
      while (rset.next ()) {
	     String w = new String(rset.getString(1));
         System.out.println (w);
	     QueryRows[ i++ ] = new String(w);
      }
      //close the result set, statement, and the connection
      rset.close();
      stmt.close();
      conn.close();

      return QueryRows;

      //return "This is from another Java class: " + sqlstmt + " " + ww;
   } 
} 
