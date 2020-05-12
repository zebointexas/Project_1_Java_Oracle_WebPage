 
/*
 * This sample can be used to check the JDBC installation.
 * Just run it and provide the connect information.  It will select
 * "Hello World" from the database.
 */
// You need to import the java.sql package to use JDBC
import java.sql.*;
// We import java.io to be able to read from the command line
import java.io.*;

class JdbcCheckup1
{
   public static void main (String args []) throws SQLException, IOException
   {
      // Load the Oracle JDBC driver
      DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
      
    Connection conn =  
           new oracle.jdbc.OracleDriver ().defaultConnection ();

       // Create a statement
       Statement stmt = conn.createStatement ();

       // Do the SQL "Hello World" thing
       ResultSet rset = stmt.executeQuery ("SELECT 'Hello World' FROM dual");

       while (rset.next ())
          System.out.println (rset.getString (1));
       System.out.println ("Your JDBC installation is correct.");
   }
}
