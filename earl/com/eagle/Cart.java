// @(#)Cart.java 09/05/24 1.24
//(C) 1987 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.io.*;
import java.util.*;
import java.text.*;

public class Cart extends Properties {
  public final static String CART_FILE = "";
  public final static String CHAT_FILE = ".jfd";
  public final static String MATCH_FILE = ".acf";
  public final static String SEARCH_FILE = ".que";

  public final static int RENDER_TEXT = -10;
  public final static int RENDER_HTML2 = 0;
  public final static int RENDER_VRML1 = 10;
  public final static int RENDER_VRML2 = 11;

  protected final static String FILE_PREFIX = "c";
  protected final static int CART_NAME_LEN = 12;
  protected final static int EXP_HOURS = 4;

  protected static Random rand = new Random();

  public String dir = null;
  public String fileName = null;
  public Date expiry = new Date();
  public int render = RENDER_HTML2;


  // Create a new, unique file name based on current date/time, as
  // "degrees", and appended with a 4-digit random number

  protected synchronized void makeName (String path) {
    dir = path;

    // add the number of degrees since January 1, 1970, 00:00:00 GMT
    // to the path name

    Degree degree = new Degree(expiry);
    fileName = FILE_PREFIX + degree.toString();

    // create the unique cart file

    File fd = null;
    String uniq = "";
    int length = 4;

    while (true) {
      uniq = Integer.toString(rand.nextInt());
      uniq = uniq.substring(uniq.length() - length, uniq.length());

      fd = new File(dir, fileName + uniq);

      if (!fd.exists())
        break;
    }

    fileName = fd.getName();

    // save something out there as a temporary placeholder

    try {
      FileOutputStream f = new FileOutputStream(fd);
      save(f, "temp");
      f.close();
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }


  // Allocate a new cart, creating a unique name within the given
  // directory path if allowed

  public Cart (String path, boolean mkName) {
    dir = path;

    if (mkName) {
      makeName(path);

      // save the required properties

      put("timestamp", DateFormat.getDateTimeInstance().format(expiry));
      put("cart", fileName);

      Calendar cal = Calendar.getInstance();
      cal.setTime(expiry);
      cal.add(Calendar.HOUR, EXP_HOURS);
      expiry = cal.getTime();

      put("expires", DateFormat.getDateTimeInstance().format(expiry));
    }
  }


  // Load a new cart based on a persistent user name

  public Cart (String path, String subDir, String user) {
    dir = path;
    fileName = subDir + File.separator + user;

    // either load the file, or create
    // it if it doesn't already exist

    if (subDir != null) {
       if (!loadFile())
          saveFile();
    }

    // save the required properties

    Date timestamp = new Date();
    put("timestamp", DateFormat.getDateTimeInstance().format(timestamp));

    put("cart", fileName);
    put("expires", "never");
  }


  // Load a new cart from an existing cart file, or create a new cart
  // file if unknown

  public Cart (String path, String fileName, boolean newHit) {
    dir = path;
    this.fileName = fileName;

    // attempt to load the file

    if (fileName != null) {
       if (!loadFile()) {
         System.err.println("Cart: can't find file " + fileName);

         if (!newHit)
	   makeName(path);
       }
    }

    // save the required properties

    put("cart", this.fileName);

    // manage the expiry date

    if (newHit) {
      put("timestamp", DateFormat.getDateTimeInstance().format(expiry));

      Calendar cal = Calendar.getInstance();
      cal.setTime(expiry);
      cal.add(Calendar.HOUR, EXP_HOURS);
      expiry = cal.getTime();

      put("expires", DateFormat.getDateTimeInstance().format(expiry));
    }

    // lookup when this cart is supposed to expire

    else if (containsKey("expires") && !getProperty("expires").equals("never")) {
      try {
	expiry = DateFormat.getDateTimeInstance().parse(getProperty("expires"));
      }
      catch (ParseException e) {
	System.err.println(e.getMessage());
	e.printStackTrace();
      }
    }
  }


  // Determine the render level for this cart

  protected void setRenderLevel () {
    if (containsKey("render")) {
      if (getProperty("render").equals("text"))
	render = RENDER_TEXT;
      else if (getProperty("render").equals("html2"))
	render = RENDER_HTML2;
      else if (getProperty("render").equals("vrml1"))
	render = RENDER_VRML1;
      else if (getProperty("render").equals("vrml2"))
	render = RENDER_VRML2;
    }
  }


  // Attempt to open an existing cart file

  public static Cart reOpen (String path, String existing) {
    Cart c = new Cart(path, false);

    try {
      FileInputStream f = new FileInputStream(new File(path, existing));

      c.fileName = existing;
      c.load(f);

      f.close();
      c.setRenderLevel();

      // lookup when this cart is supposed to expire

      if (c.containsKey("expires") && !c.getProperty("expires").equals("never"))
	c.expiry = DateFormat.getDateTimeInstance().parse(c.getProperty("expires"));
    }
    catch (ParseException p) {
      System.err.println(p.getMessage());
      p.printStackTrace();

      c = null;
    }
    catch (FileNotFoundException t) {
      c = null;
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();

      c = null;
    }

    return c;
  }


  // Return a vector of all the current cart files, based on the given
  // attributes: path and subdirectory

  public static Vector getList (String path, String subDir) {
    Vector v = new Vector();

    if (!subDir.equals(""))
      path = path.concat(File.separator + subDir);

    try {
      File f = new File(path);
      String s[] = f.list();

      for (int i = 0; i < s.length; i++) {
	if ((!subDir.equals("") ||
	    (s[i].startsWith(FILE_PREFIX) && 
	     (s[i].length() >= CART_NAME_LEN))
	     ) &&
	    !(s[i].endsWith(CHAT_FILE) || 
	      s[i].endsWith(MATCH_FILE) || 
	      s[i].endsWith(SEARCH_FILE)
	      )
	    ) {
	  v.addElement(s[i]);
	}
      }
    }
    catch (Exception e) {
      System.err.println(e);
      e.printStackTrace();
    }
    finally {
      return v;
    }
  }


  // Attempt to load the cart from a file on disk, returning false if
  // not found

  protected boolean loadFile () {
    if ((dir == null) || dir.equals(""))
      System.err.println("Cart: attempting to load file from a null directory");

    try {
      FileInputStream f = new FileInputStream(new File(dir, fileName));

      load(f);
      f.close();
      setRenderLevel();

      return true;
    }
    catch (FileNotFoundException t) {
      return false;
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();

      return false;
    }
  }


  // Attempt to save the cart file to disk

  public void saveFile () {
    if ((dir == null) || dir.equals(""))
      System.err.println("Cart: attempting to save file to a null directory");

    try {
      FileOutputStream f = new FileOutputStream(new File(dir, fileName));
      save(f, getProperty("expires"));

      f.close();
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }


  // Delete the cart file and any associated files

  public void delete () {
    File f;

    try {
      f = new File(dir, fileName + CHAT_FILE);

      if (f.exists())
	f.delete();

      f = new File(dir, fileName + MATCH_FILE);

      if (f.exists())
	f.delete();

      f = new File(dir, fileName + SEARCH_FILE);

      if (f.exists())
	f.delete();

      f = new File(dir, fileName);

      if (f.exists())
	f.delete();
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }


  // Transfer the persistent properties between two carts

  public void mingleProperties (Properties prop, boolean destructive) {
    for (Enumeration e = prop.propertyNames() ; e.hasMoreElements() ;) {
      String key = (String) e.nextElement();

      // if a property from the given source isn't defined in this
      // cart then add it

      if (!containsKey(key))
	put(key, prop.getProperty(key));

      // otherwise, update the property value, if destructive
      // overwrite is enabled

      else if (destructive)
	put(key, prop.getProperty(key));
    } 
  }


  // Get/Test/Set an integer parameter

  public int setParameter (String key, int value) {
    int result = value;

    if (containsKey(key)) {
      result = Integer.parseInt(getProperty(key));
    }
    else {
      put(key, String.valueOf(value));
    }

    return result;
  }


  // Create and return an input stream with the given extension

  public InputStream getInputStream (String extension) {
    InputStream f = null;

    try {
      f = new FileInputStream(new File(dir, fileName + extension));
    }
    catch (FileNotFoundException t) {
      f = null;
    }
    finally {
      return f;
    }
  }


  // Create and return an output stream with the given extension

  public OutputStream getOutputStream (String extension) {
    FileOutputStream f = null;

    try {
      f = new FileOutputStream(new File(dir, fileName + extension));
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
    finally {
      return f;
    }
  }
}
