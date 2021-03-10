import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import sql.java.*; 
class readwm extends application {
		Connection conn = null;
    	try {
      		Class.forName("org.sqlite.JDBC");
      		conn = DriverManager.getConnection("jdbc:sqlite:eagle.db");
    	} catch ( Exception e ) {
      		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      		System.exit(0);
    	}
    	System.out.println("Opened database successfully");

qry = open('wn.sql', 'r').read();
c = conn.cursor();
c.execute(qry);
conn.commit();
c.close();
conn.close();
}}
