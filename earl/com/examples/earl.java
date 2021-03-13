
// earl eagle rev 01.10.12 Robby Glen Garner
// (C) 1999-2012 Robby Glen Garner and Paco Xander Nathan

import com.eagle.*;
import org.jibble.jmegahal.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.*;
import java.awt.Desktop;
import java.net.URI;

public class earl extends Applet implements Runnable {

  private volatile Thread clockThread = null;
  public String person = "Unknown Host";
  InetAddress thisAddress= null;
  String displayString=null;
  static String inputBuffer;
  private static int speak = 0;  //to speak or not
  static String last_said = "";
  static String status = "";
  static String StimRez = "";
  static String earlRez = "";
  String OS = "";

  // get ready
  private static long curtime, endtime;
  private static Grammar grammar = null;
  private static EARL eagle = null;
  private static Context context = null;
  private static Cart cart = null;
  Config config = new Config(new File(System.getProperty("user.dir"), "eagle.cfg"));

  public static Random rand = new Random();

  // data contained in dialog box

  private final static boolean debug = false;
  private static Process P1;
  private static Process P2;

  public boolean inApplet = true;
  public xFrame frame = new xFrame();

  public static TextField input = new TextField("");
  public static TextArea record = new TextArea();

  // SQL specific classes
  private static Connection myConnection;
  private static Statement myStatement;
  private static ResultSet myResultSet;

  //MegaHal
  static JMegaHal hal = new JMegaHal();

  //to do math
  static ExprEval calc = new ExprEval();

  // Initialize data structures

  public void init (String[] args) {

    String dir = System.getProperty("user.dir");

    inputBuffer = "";

    // setup rules

    grammar = new Grammar();
    eagle = new EARL(grammar, true, "eagle.jrl");

    // setup cart, loading an existing cart, if given

    String cartName = "dirt";

	if (args.length > 0) {
    	try {
        	speak = Integer.parseInt(args[0]);
    	} catch (NumberFormatException e) {
        	System.err.println("Argument" + " must be an integer");
        	System.exit(1);
    	}
	}

    cart = new Cart(dir, cartName, false);
    cart.put("render", "html2");

    context = new Context(cart, eagle);
    context.loadFile();

    eagle.setVerbose(false);

    // determine whether we're running as an applet

    try {
      boolean useParam = true;
      AppletContext a = getAppletContext();
    }
    catch (NullPointerException npe) {
      inApplet = false;
    }

    inApplet = false;

    //questions.DumpLog();

    try {
      setLayout(new BorderLayout());

      record.setEditable(false);
      record.setForeground(Color.green);
      record.setBackground(Color.black);
      Font font = new Font("Courier", Font.PLAIN, 18);
      record.setFont(font);

      input.setForeground(Color.yellow);
      input.setBackground(Color.blue);
      Font font2 = new Font("Verdana", Font.PLAIN, 18);
      input.setFont(font2);

      add("Center", record);
      add("South", input);
    }
    catch (Exception e) {
      System.err.println("Is your windowing system running?");
      System.exit(1);
    }


    input.addActionListener(

       new ActionListener() //anonymous inner class
       {
            // event handler called when saveCustomerButton is clicked
            public void actionPerformed( ActionEvent event )
            {
                 sendLine();
            }

       } // end anonymous inner class

    ); // end call to addActionListener


    // initialize frame, if needed

    if (inApplet) {
      setVisible(true);
    }
    else {
      frame.setTitle("earl console");
      frame.add("Center", this);
      frame.pack();
      frame.setSize(900, 500);
      frame.setVisible(true);
    }


    try{
        thisAddress = InetAddress.getLocalHost();
        person=new String(thisAddress.getHostName());
    }
        catch(UnknownHostException e) {
           person="unknown host";
    }

    //setupDatabaseConnection();

    System.out.print("loading tight sponge...");

    try {

	hal.addDocument("file:///Users/robby/documents/earl/sponge.txt");

    }
      catch (IOException e) {
	  System.err.println("Hal: I/O error - " + e.getMessage());
	  e.printStackTrace();
    }

    System.out.println("Finished.\n");


        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("chatlog.txt", true));

            out.write(bigTime(1) + "\n");
            out.write("[begin]" + "\n");

            out.write("\n");
            out.close();

        } catch (IOException e) {
        }


    displayString = new String("Operating.\n" + person + " connected.\n\nOperating System: " +  System.getProperty("os.name") + "." );
    recordLine(displayString);
    input.setText("");
    input.requestFocus();

  }


	public static void setupDatabaseConnection()
    {

		Connection c = null;
    	try {
      		Class.forName("org.sqlite.JDBC");
      		c = DriverManager.getConnection("jdbc:sqlite:eagle.db");
    	} catch ( Exception e ) {
      		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      		System.exit(0);
    	}
    	System.out.println("Opened database successfully");

	} // end setUpDatabaseConnection


  public static String stringreplace(String source, String pattern, String replace)
      {
          if (source!=null)
          {
          final int len = pattern.length();
          StringBuffer sb = new StringBuffer();
          int found = -1;
          int start = 0;

          while( (found = source.indexOf(pattern, start) ) != -1) {
              sb.append(source.substring(start, found));
              sb.append(replace);
              start = found + len;
          }

          sb.append(source.substring(start));

          return sb.toString();
          }
          else return "";
    }


  private static String formQuest(String inputLine)
  {

       String topic = "";
       URL url = null;
       String queryString = "";
       String glossaryDescription = "";

       String query[]= {"what is ",
			"what are the ",
			"tell me about ",
			"define ",
			"have you seen ",
			"what happened in ",
			"what's a ",
			"is that a ",
			"is this a ",
			"have you ever heard of ",
			"do you know of ",
			"do you know about ",
            		"what is the meaning of ",
			"end-of-list"};

	    setupDatabaseConnection();

        int i, gotAt;
        for (i=0; !query[i].equals("end-of-list"); i++) {
           gotAt=inputLine.indexOf(query[i]);
           if (gotAt>=0) {
              topic=inputLine.substring(gotAt + query[i].length());
           }
        }

        if (topic.length()>0) {

            queryString = "select wn_gloss.gloss from wn_gloss, wn_synset where wn_gloss.synset_id = wn_synset.synset_id and wn_synset.word = '" + topic + "';";

            try
            {

               myResultSet = myStatement.executeQuery( queryString );
               int decounter = 1;
               while ( myResultSet.next() )
               {

                   glossaryDescription += decounter + ":" + myResultSet.getString( "gloss" ) + "\n\n";
                   decounter++;

               }

               myResultSet.close();

            }
            catch( SQLException exception )
            {
                    exception.printStackTrace();
            }

        }

        return glossaryDescription;

  }


  static String callEARL(String stimulus) {

       // perform initial parsing

       Phrase phrase = new Phrase(stimulus, grammar.breaks);

       grammar.removeCants(phrase);
       grammar.removeFluff(phrase);

       phrase.stashExpand();

       // formulate a response

       String earlResponse = eagle.formReply(phrase, context, true);

       // save the current context

       cart.saveFile();
       context.saveFile();

       return earlResponse;
  }


  // Send a line of text to the EARL

  public static void sendLine () {
    String links[] = null;
    String stimulus = "";
    String response = "";
    boolean found = false;
    String simpleDef = "";
    int selflimit;
    double rezzy;

    stimulus = input.getText();

    input.setText("");


    if (stimulus.startsWith("!")) {
	   Save_Stimrez(last_said, stimulus.substring(1));
    }
    else if (status.indexOf("[unknown]") != -1)
    {
	System.out.println("got here " + last_said + " | " + stimulus);
	Save_Stimrez(last_said, stimulus);
    }

     String lowput = stimulus.toLowerCase();
     lowput = stringreplace(lowput, ".", "");
     lowput = stringreplace(lowput, "!", "");
     lowput = stringreplace(lowput, "?", "");

     makeRelate(lowput);

     String response2 = getRelate(lowput);

     if (response2.equals("")) makeRelate(lowput);
     else updateRelate(lowput);
     selflimit = 0;
     while ((!response2.equals("")) && (selflimit < 8)) {
       selflimit++;
       if (response.equals(""))
   	   {
	     response = response2;
	   }
	   else {

	     response += " ";
		 response += response2;
       }

         response2 = getRelate("what is " + response2);

         if (!response2.equals("")) response += ",";
     }


     rezzy=0;

	 try {
         if ((lowput.startsWith("what is ")) && (hasNumber(lowput))) {
             if (lowput.startsWith("what is ")) {
                 rezzy = calc.evaluate(lowput.substring(8));
             } else rezzy = calc.evaluate(lowput);
             response = String.valueOf(rezzy);
         }
     } catch (ExprEvalException e) {
         System.err.println("problem with calculator!");
         response = "Bad math syntax.";
     }

     String halspeaks = hal.getSentence(stimulus);
     if (halspeaks.endsWith(" the")) halspeaks += " thing.";
     if (halspeaks.endsWith(" a")) halspeaks += " reasonable measure.";
     if (halspeaks.endsWith(" and")) halspeaks += " so on.";
     if (halspeaks.endsWith(" there")) halspeaks += " you go.";
     if (halspeaks.endsWith(" of")) halspeaks += " them.";
     if (halspeaks.startsWith("the ")) halspeaks = "I can tell " + halspeaks;


     if (lowput.equals("quiet")) speak = 0;
     if (lowput.equals("speak aloud")) speak = 1;
     //if (lowput.equals("stop")) P2.destroy();

     String theTime;
     theTime = "The current time is " + bigTime(2);
     String theDate;
     theDate = bigTime(1);
     String bigDate;
     bigDate = "Today is " + bigDay() + " " + bigTime(1);


     record.append(">" + stimulus + "\n");

     last_said = lowput;


    if (lowput.indexOf("fortune") != -1) response = getFortune();
    if (lowput.startsWith("wiki")) wiki(stimulus);
    if (lowput.startsWith("google")) google(stimulus.replace(" ", "+"));
    if (lowput.startsWith("lookup ")) mathematics(stimulus.substring(7));

    //String fq = formQuest(stimulus);
    //if (!fq.equals("")) response = fq;

     StimRez = Stimrez_Response(lowput);
     if ((StimRez != null) && !StimRez.equals("") && (StimRez.indexOf(" ") != -1)) {
		if (response.equals(""))
		 {
			response = StimRez;
		 }
		 else { response += " " + StimRez; }
	}

    earlRez = callEARL(stimulus);
    if (response.equals("")) {
    	response = earlRez;
    	status = earlRez;
    }
    else response = earlRez += " " + response;



    if (response.equals("")) response = "Working."; // halspeaks;
    if (response.indexOf("[exit]") != -1) {


        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("chatlog.txt", true));

            out.write("[end]" + "\n");
            out.write(bigTime(1) + "\n");

            out.write("\n");
            out.close();

        } catch (IOException e) {
        }
        System.exit(0);
    }
    if (response.indexOf("[unknown]") != -1) response = stringreplace(response, "[unknown]", halspeaks);
    if (response.indexOf("[theTime]") != -1) response = stringreplace(response, "[theTime]", theTime);
    if (response.indexOf("[theDate]") != -1) response = stringreplace(response, "[theDate]", theDate);
    if (response.indexOf("[bigDate]") != -1) response = stringreplace(response, "[bigDate]", bigDate);
    if (response.indexOf("[AGE]") != -1) response = stringreplace(response, "[AGE]", "I was born on July 7, 1997 in Austin Texas.");

    //****send it!!!!*******
    if (!response.equals("")) {
       recordLine(response.trim());
    }

	curtime = System.currentTimeMillis();
	//endtime = curtime + response.length() * 125;
  //  endtime = curtime + 4000;

//    	do {
//    			curtime = System.currentTimeMillis();
//    	} while (curtime < endtime);

    // go display the URL's

    links = context.getLinks();

    if (links != null) {
       for (int i = 0; i < links.length; i++) {
           browser_spawn(links[i]);
       }
    }


        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("chatlog.txt", true));

            out.write(">" + stimulus + "\n");
            out.write(response + "\n");

            out.write("\n");
            out.close();

        } catch (IOException e) {
        }

  }

   private static String bigTime(int selForm) {

	java.util.Date d = new java.util.Date();

	DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	DateFormat df2 = DateFormat.getTimeInstance(DateFormat.SHORT);

	String s1 = df1.format(d);
	String s2 = df2.format(d);

	if (selForm == 1) return s1;
	else return s2;
    }


 private static String bigDay() {

     GregorianCalendar newCal = new GregorianCalendar( );

     int day = newCal.get( Calendar.DAY_OF_WEEK );

	  if (day == 7) return "Saturday";
	  if (day == 6) return "Friday";
	  if (day == 5) return "Thursday";
	  if (day == 4) return "Wednesday";
	  if (day == 3) return "Tuesday";
	  if (day == 2) return "Monday";
	  if (day == 1) return "Sunday";
	  return "... I don't know what day it is.";

 }



  public static void browser_spawn(String com) {
     System.out.println("open page: " + com);

     URL url = null;

     if (com.startsWith("!")) {
          try {
            P1 = Runtime.getRuntime().exec(com.substring(1) );
	      }
          catch (IOException e)
	      {
	        System.err.println("I am unable to comply due to technical difficulties.");
          }
     }
     else
        if (com.startsWith("@")) {
             try {
                 P2 = Runtime.getRuntime().exec("mpg123 " + com.substring(1) );
             }
             catch (IOException e)
             { System.err.println("I am unable to comply due to technical difficulties.");
             }
       }
     else
     {
          try {
              URI uri= new URI(com);
	//	         if (System.getProperty("os.name").equals("Mac OS X")) MRJFileUtils.openURL(com);
	//	         else P1 = Runtime.getRuntime().exec("C:/Program Files/Internet Explorer/iexplore.exe " + com);


          }
             catch (Exception e) {
             System.err.println(e);
             e.printStackTrace();
            }
     }

  }

 public static String getFortune() {

    String result="";

      try {
           P1 = Runtime.getRuntime().exec( "/Users/robby/getFortune"  );
       }
       catch (IOException e)
	   {
	       System.err.println("I have no mouth and I must scream!");
       }

  	 BufferedReader in = null;

    	try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/Users/robby/fortune.txt"))));

     	String s;

      	System.out.print("\nLoading Fortune. ");

      	// read each line

      	while ((s = in.readLine()) != null) {
			result += s + " ";
	 	}
     }
    catch (IOException e) {
      System.err.println("Fortune: I/O error - " + e.getMessage());
      e.printStackTrace();
    }

	 return result;

}

public static String execCmd(String cmd) {

    String comline = "sam " + new String(cmd);

    try {
         P1 = Runtime.getRuntime().exec(comline);
         P1.waitFor();
    }
    catch (IOException e)
    {
      System.err.println("Twelve Oh One alarm.");
    }
    catch (InterruptedException e)
    {
      System.err.println("We have a problem Houston.");
    }

    java.io.InputStream is = P1.getInputStream();
    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
    String val = "";
    if (s.hasNext()) {
        val = s.next();
    }
    else {
        val = "";
    }
    return val;
}


    private static boolean hasNumber(String input) {
        String numeric = "0123456789";
        boolean retval = false;

        for (int i=0; i < input.length(); i++) {
            if (numeric.indexOf(input.substring(i,i+1)) > -1) retval = true;
        }
        return retval;
    }


  public static void mathematics(String inputLine)
  {
       String topic = "";
       URL url = null;
       String queryString = "";

        if (inputLine.length()>0) {

	    queryString = "http://www.google.com/search?hl=en&ie=ISO-8859-1&q=" + inputLine.replace(' ', '+');
            try {
                 browser_spawn(queryString);

            }
            catch (Exception e) {
               System.err.println(e);
               e.printStackTrace();
            }

        }
  }


  public static void wiki(String inputLine)
  {
       String topic = "";
       URL url = null;
       String queryString = "";

        if (inputLine.length()>0) {

	    queryString = "http://en.wikipedia.org/wiki/" + inputLine.substring(5).replace(' ', '_');
            try {

		        browser_spawn(queryString);

            }
            catch (Exception e) {
               System.err.println(e);
               e.printStackTrace();
            }

        }
  }


  public static void google(String inputLine)
  {
       String topic = "";
       URL url = null;
       String queryString = "";

        if (inputLine.length()>0) {

	    queryString = "https://www.google.com/#q=" + inputLine.substring(7);
            try {

		        browser_spawn(queryString);

            }
            catch (Exception e) {
               System.err.println(e);
               e.printStackTrace();
            }

        }
  }


  private static String Stimrez_Response(String goal)
  {
		Connection c = null;
    	try {
      		Class.forName("org.sqlite.JDBC");
      		c = DriverManager.getConnection("jdbc:sqlite:eagle.db");
    	} catch ( Exception e ) {
      		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      		System.exit(0);
    	}

      String response = "";
      String queryString = "select stimulus, response from wonder where stimulus like \"%" + goal + "%\";";
      ArrayList tricorder = new ArrayList();

      try
        {
          myStatement = c.createStatement();
          myResultSet = myStatement.executeQuery(queryString);

	  while (myResultSet.next())
	  {

	      tricorder.add(myResultSet.getString("response"));
	  }

            myStatement.close();
      		//c.commit();
      		c.close();

          }
          catch( SQLException exception )
          {
              exception.printStackTrace();
          }
      	  if (tricorder.size() > 0) response = tricorder.get(rand.nextInt(tricorder.size())) + " ";
	      else response = null;

      if (response == null)
      {
		  response = "";
      }

      //System.out.println("Ack! " + response);

	  return response;
  }


  public static void Save_Stimrez(String stimulus, String response)
  {
		Connection c = null;
    	try {
      		Class.forName("org.sqlite.JDBC");
      		c = DriverManager.getConnection("jdbc:sqlite:eagle.db");
    	} catch ( Exception e ) {
      		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      		System.exit(0);
    	}

            String saveNewStimrezSQL =
                    "INSERT INTO wonder ( stimulus, response ) VALUES (\"" + stimulus + "\",\"" + response + "\");";


            try
            {
                    myStatement = c.createStatement();
                    myStatement.executeUpdate( saveNewStimrezSQL );
                    myStatement.close();
      	            c.commit();
      		    c.close();
            }
            catch( SQLException exception )
            {
                    //exception.printStackTrace();
            }

      System.out.println("saving a wonderful record");
  }


    public static void makeRelate(String inputline)
    {

	String lhand = "";
	String rhand = "";
	String pivot = "";

	Connection c = null;
    	try {
      		Class.forName("org.sqlite.JDBC");
      		c = DriverManager.getConnection("jdbc:sqlite:eagle.db");
    	} catch ( Exception e ) {
      		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      		System.exit(0);
    	}


        int gotIt = inputline.indexOf(" is ");
        if (gotIt != -1) {
            lhand = inputline.substring(0, gotIt);
            rhand = inputline.substring(gotIt + 4, inputline.length());
            pivot = "is";

            String saveNewRelationSQL =
                    "INSERT INTO Relate " +
                    "( lhand, pivot, rhand ) " +
                    "VALUES " +
                    "(  '" + lhand + "'," +
                       "'" + pivot + "'," +
                       "'" + rhand + "');";

            try
            {

		myStatement = c.createStatement();

                // this is where we actually execute the query against the DB
                myStatement.executeUpdate( saveNewRelationSQL );
                myStatement.close();
      		c.commit();
      		c.close();

                // if this line prints then everything went ok
                // System.out.println( "Saving: " + lhand + " : " + pivot + " : " + rhand );
            }
            catch( SQLException exception )
            {
		//System.out.println("Record already exists.");
		updateRelate(inputline);
            }
        }

        gotIt = inputline.indexOf(" am ");
        if (gotIt != -1) {
	    lhand = inputline.substring(0, gotIt);
            rhand = inputline.substring(gotIt + 4, inputline.length());
            pivot = "am";

            String saveNewRelationSQL =
                    "INSERT INTO Relate " +
                    "( lhand, pivot, rhand ) " +
                    "VALUES " +
                    "(  '" + lhand + "'," +
                       "'" + pivot + "'," +
                       "'" + rhand + "');";


            try
            {
		myStatement = c.createStatement();

                // this is where we actually execute the query against the DB
                myStatement.executeUpdate( saveNewRelationSQL );
                myStatement.close();
      		c.commit();
      		c.close();
            }
            catch( SQLException exception )
            {
		//System.out.println("Record already exists.");
                updateRelate(inputline);
            }
        }


        gotIt = inputline.indexOf(" are ");

        if (gotIt != -1) {
	    lhand = inputline.substring(0, gotIt);
            rhand = inputline.substring(gotIt + 5 , inputline.length());
            pivot = "are";

            String saveNewRelationSQL =
                    "INSERT INTO Relate " +
                    "( lhand, pivot, rhand ) " +
                    "VALUES " +
                    "(  '" + lhand + "'," +
                       "'" + pivot + "'," +
                       "'" + rhand + "');";


            try
            {
		myStatement = c.createStatement();

                // this is where we actually execute the query against the DB
                myStatement.executeUpdate( saveNewRelationSQL );
                myStatement.close();
      		c.commit();
      		c.close();
            }
            catch( SQLException exception )
            {
		//System.out.println("Record already exists.");
		updateRelate(inputline);
            }
        }


    }


    public static void updateRelate(String inputline)
    {
	Connection c = null;
    	try {
      		Class.forName("org.sqlite.JDBC");
      		c = DriverManager.getConnection("jdbc:sqlite:eagle.db");
    	} catch ( Exception e ) {
      		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      		System.exit(0);
    	}

	String lhand = "";
	String rhand = "";
	String pivot = "";

        int gotIt = inputline.indexOf(" is ");
        if (gotIt != -1) {
	    lhand = inputline.substring(0, gotIt);
            rhand = inputline.substring(gotIt + 4, inputline.length());
            pivot = "is";

            String updateNewRelationSQL =
                    "UPDATE Relate " +
                    "SET  " +
                    "rhand = '" + rhand + "' where lhand = '" + lhand + "';";

            try
            {
		myStatement = c.createStatement();
                // this is where we actually execute the query against the DB
                myStatement.executeUpdate( updateNewRelationSQL );
                myStatement.close();
      		c.commit();
      		c.close();
                // if this line prints then everything went ok
                //System.out.println( "Updating: " + lhand + " : " + pivot + " : " + rhand );
            }
            catch( SQLException exception )
            {
	            //System.out.println("Update was unsuccessful.");
            }
        }

        gotIt = inputline.indexOf(" am ");
        if (gotIt != -1) {
	   lhand = inputline.substring(0, gotIt);
           rhand = inputline.substring(gotIt + 4, inputline.length());
           pivot = "am";

            String updateNewRelationSQL =
                    "UPDATE Relate " +
                    "SET  " +
                    "rhand = '" + rhand + "' where lhand = '" + lhand + "';";

            try
            {
                    myStatement = c.createStatement();
                    // this is where we actually execute the query against the DB
                    myStatement.executeUpdate( updateNewRelationSQL );
                    myStatement.close();
      	            c.commit();
      	            c.close();
                    // if this line prints then everything went ok
                    //System.out.println( "Updating: " + lhand + " : " + pivot + " : " + rhand );
            }
            catch( SQLException exception )
            {
		            //System.out.println("Update was unsuccessful.");
            }
        }


        gotIt = inputline.indexOf(" are ");
        if (gotIt != -1) {
	   lhand = inputline.substring(0, gotIt);
           rhand = inputline.substring(gotIt + 5, inputline.length());
           pivot = "are";

            String updateNewRelationSQL =
                    "UPDATE Relate " +
                    "SET  " +
                    "rhand = '" + rhand + "' where lhand = '" + lhand + "';";

            try
            {
			   myStatement = c.createStatement();
               // this is where we actually execute the query against the DB
               myStatement.executeUpdate( updateNewRelationSQL );
               myStatement.close();
      	       c.commit();
      	       c.close();
               // if this line prints then everything went ok
               //System.out.println( "Updating: " + lhand + " : " + pivot + " : " + rhand );
            }
            catch( SQLException exception )
            {
		       //System.out.println("Update was unsuccessful.");
            }
        }


    }


    public static String getRelate(String inputline)
    {
		Connection c = null;
    	try {
      		Class.forName("org.sqlite.JDBC");
      		c = DriverManager.getConnection("jdbc:sqlite:eagle.db");
    	} catch ( Exception e ) {
      		System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      		System.exit(0);
    	}

		String lhand = "";
		String rhand = "";
		String pivot = "";
		String retval = "";

        int gotIt = inputline.indexOf(" is ");
        if (gotIt != -1) {
	    lhand = inputline.substring(0, gotIt);
            rhand = inputline.substring(gotIt + 4, inputline.length());
            pivot = "is";

            String selectRelateSQL =
                    "SELECT LHAND, PIVOT, RHAND " +
                    "FROM RELATE " +
                    "WHERE " +
                    "LHAND = '" + rhand + "';";

            //System.out.println(selectRelateSQL);

            try
            {

                    myStatement = c.createStatement();
                    myResultSet = myStatement.executeQuery( selectRelateSQL );

                    while ( myResultSet.next() )
                    {
                        retval = myResultSet.getString( "RHAND" ).trim();
                    }

                    myStatement.close();
      		    c.commit();
      		    c.close();
                    myResultSet.close();

                    // if this line prints then everything went ok
                    //System.out.println( "Retrieve relate successful" );
            }
            catch( SQLException exception )
            {
				//System.out.println("Relationship not found.");
            }

	}

        gotIt = inputline.indexOf(" am ");
        if (gotIt != -1) {
	    lhand = inputline.substring(0, gotIt);
            rhand = inputline.substring(gotIt + 4, inputline.length());
            pivot = "am";

            String selectRelateSQL =
                    "SELECT lhand, pivot, rhand " +
                    "FROM Relate " +
                    "WHERE " +
                    "lhand = '" + rhand + "';";


            try
            {

                    myStatement = c.createStatement();
                    myResultSet = myStatement.executeQuery( selectRelateSQL );

                    while ( myResultSet.next() )
                    {
                        retval = myResultSet.getString( "right_hand" ).trim();
                    }
                    myStatement.close();
		    c.commit();
      		    c.close();
                    myResultSet.close();

                    // if this line prints then everything went ok
                    //System.out.println( "Retrieve relate successful" );
            }
            catch( SQLException exception )
            {
				//System.out.println("Relationship not found.");
            }

	}


        gotIt = inputline.indexOf(" are ");
        if (gotIt != -1) {
	    lhand = inputline.substring(0, gotIt);
            rhand = inputline.substring(gotIt + 5, inputline.length());
            pivot = "are";

            String selectRelateSQL =
                    "SELECT lhand, pivot, rhand " +
                    "FROM RELATE " +
                    "WHERE " +
                    "lhand = '" + rhand + "';";


            try
            {

                    myStatement = c.createStatement();
                    myResultSet = myStatement.executeQuery( selectRelateSQL );

                    while ( myResultSet.next() )
                    {
                        retval = myResultSet.getString( "rhand" ).trim();
                    }
                    myStatement.close();
      				c.commit();
      				c.close();
                    myResultSet.close();

                    // if this line prints then everything went ok
                    //System.out.println( "Retrieve relate successful" );
            }
            catch( SQLException exception )
            {
				//System.out.println("Relationship not found.");
            }

	}

        //System.out.println("[" + retval + "]");

	return retval;
}


  // Record a line of text from the EARL

  public static void recordLine (String line) {
    String text = record.getText();
    String processed_line = "";
    String sub = "";
    int dis = 0;

    line = stringreplace(line, "null", "it");

    if (line.length() <= 70) {
       processed_line = line;
    }
    else {
           for (int k=0; (k < (line.length())); k++) {
               if ((line.charAt(k)==' ') && (dis>70)) {
                   dis=0;
                   processed_line=processed_line.concat(sub + "\r\n");
                   sub="";
               }
               else {
                       dis++;
                       if (line.charAt(k)=='\n') {
                           dis = 0;
                       }
                       sub=sub.concat(line.substring(k,k+1));
               }
           }
           processed_line=processed_line.concat(sub + "\r\n");
    }

    record.append(processed_line);
    record.append("\n\n");
    text = record.getText();
    record.setSelectionStart(text.length());
    record.setSelectionEnd(text.length());

      if (speak==1) {
      try {
	       P1 = Runtime.getRuntime().exec( "/usr/bin/say \" " + line + "\"" );

	      //P1.waitFor();
       }
       catch (Exception err)
	   {
	       System.err.println("I have no mouth and I must scream! (text-to-speech malfunction)");
       }
    }
    System.out.println("Speech Complete.");

  }

	private static class runP implements Runnable
	{
            private volatile Thread runpThread = null;
            private String cpass;
            private String latch = "open";
            private String latch2 = "open";
            public int cycles = 0;

            public void start() {
                if (runpThread == null) {
                    runpThread = new Thread(this, "runP");
                    runpThread.start();
                }
            }

            public void run() {

                int timelag = 0;
                int silence = 0;
                int curlen = 0;

                while (true) {

                cycles++;

                try {
                     Thread.sleep(1000);
                     if (timelag > 4) {
                         timelag = 0;
                         sendLine();
                     }
                     if (!latch2.equals("open") && (silence > 60)) {
                         recordLine("Take your time. I'm in no hurry.");
                         silence = 0;
                         latch2 = "open";
                     }

                     cpass = input.getText();
                     if (!cpass.equals("")) {
                        timelag++;
                        silence = 0;
                        if (latch.equals("open")) {
                        	latch2 = "closed";
                        	latch = "closed";
                        }
                        if (cpass.length() > curlen) timelag = 0;
                        curlen = cpass.length();
                        if (latch.equals("open") && (curlen > 30) && (timelag > 5)) {
                            recordLine("Guess what?");
                            timelag = 0;
                            latch = "closed";

                        }
                        if (cpass.startsWith("fuc")) {
                            recordLine("Hey! I'll have none of that!");
                            input.setText("");
                        }
                        if (timelag > 6) {
                            if (cpass.charAt(cpass.length()-1) == '.') sendLine();
                            //if (cpass.charAt(cpass.length()-1) == '!') sendLine();
                            if (cpass.charAt(cpass.length()-1) == '?') sendLine();
                        }
                        if (timelag > 4) if (cpass.charAt(cpass.length()-1) == '!') sendLine();
                     }
                     else {
                            timelag = 0;
                            silence++;
                     }
                } catch (InterruptedException e){ }
                }
            }
	}


  public void run () {
  }

  public void start () {
       if (clockThread == null) {
         clockThread = new Thread(this, "Clock");
         clockThread.start();
      }


  }

  public void stop () {

     clockThread = null;

  }


  // Console Operation

  public static void main (String[] args) {
    runP theg = new runP();
    Thread taskP = new Thread(theg);
    taskP.start();

    earl aleph = new earl();
    aleph.init(args);
    aleph.start();
  }
}
