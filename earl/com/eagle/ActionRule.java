// @(#)ActionRule.java 09/07/01 1.11 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.util.*;

public class ActionRule extends Rule {
  private int sel = 0;
  public int sum = 0;
  public int priority = 0;

  public Vector url = new Vector();
  public Vector expect = new Vector();


  public ActionRule (String name) {
    super(name);
  }


  // Re-randomize the order of actions

  public void shuffle () {
    try {
      sel = Math.abs(EARL.rand.nextInt()) % size();
    }
    catch (Exception e) {
      System.err.println(e.getMessage());
      e.printStackTrace();

      System.err.println("ActionRule: no actions for rule " + name);
      sel = 0;
    }
  }


  // Establish the rule priority

  public void setPriority () {
    if (hasProperty(Rule.PROP_PRIORITY)) {
      priority = Integer.parseInt(getProperty(Rule.PROP_PRIORITY));
    }
  }


  // Select within circular queue of response templates,
  // making the queue index wrap, if needed

  public int select () {
    int result = sel++;

    if (sel == size()) {
      sel = 0;
    }

    return result;
  }


  // Return the next action, in order

  public String nextAction (String s, Properties p) {
    String selected = (String) elementAt(select());
    StringTokenizer st = new StringTokenizer(selected, " ", true);
    StringBuffer buf = new StringBuffer(200);

    while (st.hasMoreTokens()) {
      String token = st.nextToken();

      if (token.equals("$stimulus")) {
	token = s;
      }
      else if (token.startsWith("$")) {
	token = p.getProperty(token.substring(1));
      }

      buf.append(token);
    }

    return buf.toString();
  }
}
