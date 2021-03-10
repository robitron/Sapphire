// @(#)Rule.java 98/06/29 1.13 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.util.*;

public class Rule extends Vector {
  public final static String TYPE_ACTION = "action:";
  public final static String TYPE_FUZZY = "fuzzy:";
  public final static String TYPE_INTRO = "intro:";
  public final static String TYPE_REGEX = "regex:";
  public final static String TYPE_RESPONSE = "response:";

  public final static String PROP_ABOUT = "about";
  public final static String PROP_EQUALS = "equals";
  public final static String PROP_BIND = "bind";
  public final static String PROP_INVOKES = "invokes";
  public final static String PROP_HTML = "html";
  public final static String PROP_NEXT = "next";
  public final static String PROP_PRIORITY = "priority";
  public final static String PROP_REPEAT = "repeat";
  public final static String PROP_REQUIRES = "requires";
  public final static String PROP_TELLME = "tellme";
  public final static String PROP_URL = "url";
  public final static String PROP_EXPECT = "expect";
  public final static String PROP_SETVAR = "setvar";

  protected Properties prop = new Properties();
  protected String name = "";


  // Constructors

  public Rule () {
    super();
  }

  public Rule (String name) {
    super();
    this.name = name.trim();
  }


  // Access the rule name

  public String getName () {
    return name;
  }


  // Test for the given property

  public boolean hasProperty (String key) {
    return prop.containsKey(key);
  }


  // Test for a name/value pair equivalence within the "dirt" context

  public boolean testProperty (String pair, Properties p) {
    if (pair != null) {
      TSVector tsv = new TSVector(pair, " ", false, false);

      String equ = tsv.pop().trim();
      String val = pair.substring(equ.length() + 1).trim();

      return p.containsKey(equ) && val.equals(p.getProperty(equ).trim());
    }

    return true;
  }


  // Access the given property

  public String getProperty (String key) {
    if (hasProperty(key)) {
      return prop.getProperty(key);
    }
    else {
      return null;
    }
  }


  // Add the key/value pair to the rule's properties

  public void putProperty (String key, String value) {
    prop.put(key.trim(), value.trim());
  }
}
