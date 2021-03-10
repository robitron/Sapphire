// @(#)Config.java 09/12/18 1.2 Paco Xander Nathan

package com.eagle;
import java.io.*;
import java.util.*;

public class Config extends Properties {

  public Config (File f) {
    if ((f != null) && f.exists()) {
      if (!loadFile(f))
	System.err.println("Config: " + f.getName() + " not found");
    }

    setDefault("user.dir", System.getProperty("user.dir"));
  }


  // Access methods

  public void setDefault (String key, Object value) {
    if (!containsKey(key))
      setValue(key, value);
  }

  public void setValue (String key, Object value) {
    if (value instanceof String)
      put(key, value);
    else
      put(key, ((Integer) value).toString());
  }

  public String getString (String key) {
    return getProperty(key);
  }

  public int getInt (String key) {
    return Integer.parseInt(getProperty(key));
  }


  // Attempt to load from a file on disk, returning false if not found

  public boolean loadFile (File file) {
    try {
      FileInputStream f = new FileInputStream(file);
      load(f);

      return true;
    }
    catch (FileNotFoundException t) {
      return false;
    }
    catch (IOException e) {
      System.err.println("Config: " + e.getMessage());
      e.printStackTrace();

      return false;
    }
  }


  // Attempt to save the file to disk

  public void saveFile (File file) {
    try {
      FileOutputStream f = new FileOutputStream(file);
      save(f, "configuration file");
    }
    catch (IOException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }


  // Test interface

  public static void main (String[] args) {
    Config c = new Config(new File(System.getProperty("user.dir"), "test.cfg"));

    for (Enumeration e = c.propertyNames() ; e.hasMoreElements() ;) {
      String name = (String) e.nextElement();
      System.out.println(name + " = " + c.getProperty(name));
    }
  }
}
