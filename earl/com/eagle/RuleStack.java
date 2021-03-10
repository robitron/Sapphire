// @(#)RuleStack.java 97/12/18 1.3
// (C) 1999 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.io.*;
import java.util.*;

public class RuleStack extends Stack {

  // Test whether the given rule is already a member of this stack

  public boolean hasRule (Rule r) {
    for (Enumeration e = elements(); e.hasMoreElements(); ) {   
      Rule test = (Rule) e.nextElement();

      if (test.getName().equals(r.getName()))
	return true;
    }

    return false;
  }


  // Remove the given rule from the stack

  public void removeRule (Rule r) {
    removeElement(r);
  }


  // Add another rule to the stack -- but remove any prior instance

  public void addRule (Rule r) {
    if (r != null) {
      if (hasRule(r))
	removeRule(r);

      push(r);
    }
  }


  // Load a rule stack from a text file

  public void loadRules (BufferedReader f, Hashtable actsHash) {
    try {
      String lineIn;

      if ((lineIn = f.readLine()) == null)
	return;

      removeAllElements();
      int size = Integer.parseInt(lineIn);

      for (int i = 0; i < size; i++) {
	if ((lineIn = f.readLine()) == null)
	  return;

	ActionRule r = (ActionRule) actsHash.get(lineIn.trim());
	addRule(r);
      }

      // trim the stack depths

      int max = 50;

      for (int i = 0; i < (size() - max); i++)
	removeElementAt(0);
    }
    catch (IOException e) {
      return;
    }
  }


  // Save a rule stack to a text file

  public void saveRules (PrintWriter out) {
    out.println(size());

    for (int i = 0; i < size(); i++) {
      ActionRule r = (ActionRule) elementAt(i);
      out.println(r.getName().trim());
    }
  }
}
