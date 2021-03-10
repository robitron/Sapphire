import java.sql.*;

public class DBcreateRelate
{
  public static void main( String args[] )
  {
    Connection c = null;
    Statement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:eagle.db");
      System.out.println("Opened database successfully");

      stmt = c.createStatement();
      String sql = "CREATE TABLE RELATE " +
                   "(LHAND CHAR(50) PRIMARY KEY     NOT NULL," +
                   " PIVOT          CHAR(25)    NOT NULL, " + 
                   " RHAND        CHAR(50))";
      stmt.executeUpdate(sql);
      stmt.close();
      c.close();
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    System.out.println("Table created successfully");
  }
}

