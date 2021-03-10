//Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.util.*;

public class TSVector extends Vector {
  protected String breaks = "\t";


  public TSVector (String s, String breaks, boolean initTab, boolean isVerbose) {
    StringTokenizer st = new StringTokenizer(s, breaks, true);
    boolean lastBlank = true;

    // iterate over each possible token, filtering tabs
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      boolean addIt = true;

      if (breaks.indexOf(token) < 0) {
	lastBlank = false;
      }
      else if (lastBlank) {
	token = new String("");
      }
      else {
	addIt = false;
	lastBlank = true;
      }

      if (addIt) {
	if (isVerbose)
	  System.out.println(token);

	addElement(token);
      }
    }
  }


  public String pop () {
    String s = "";

    if (size() > 0) {
      s = (String) elementAt(0);
      removeElementAt(0);
    }

    return s;
  }
}
