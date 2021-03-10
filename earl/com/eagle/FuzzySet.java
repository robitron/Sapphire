// @(#)FuzzySet.java 09/12/14 1.2 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.util.*;

public class FuzzySet extends Object {
  public ActionRule rule = null;
  public int prob = 0;
  protected String ref = null;

  // Constructors

  public FuzzySet (int prob, String ref) {
    this.ref = ref;
    this.prob = prob;
  }

  public FuzzySet (int prob, ActionRule rule) {
    this.rule = rule;
    this.prob = prob;
  }


  // Use the "ref" string to link into a compiled ActionRule

  public void linkRule (Hashtable actsHash) {
    if (ref != null) {
      rule = (ActionRule) actsHash.get(ref);

      if (rule != null)
	ref = null;
      else
	System.err.println("FuzzySet: rule " + ref + " not found.");
    }
  }
}
