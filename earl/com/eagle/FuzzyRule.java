// @(#)FuzzyRule.java 09/12/14 1.5 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.util.*;

public class FuzzyRule extends Rule {

  public FuzzyRule (String name) {
    super(name);
  }


  // Add a new fuzzy set within the list

  public void addElement (int prob, String rule) {
    addElement(new FuzzySet(prob, rule));
  }


  // Normalize the fuzzy sets within the list

  public void normalize (Hashtable actsHash) {
    int totProb = 0;

    // relink rules, based on the given hashtable, then find the total
    // probability for this fuzzy set

    for (int i = 0; i < size(); i++) {
      FuzzySet f = (FuzzySet) elementAt(i);

      f.linkRule(actsHash);
      totProb += f.prob;
    }

    // normalize from 0-100

    for (int i = 0; i < size(); i++) {
      FuzzySet f = (FuzzySet) elementAt(i);
      f.prob = (int) Math.round(100.0 * ((double) f.prob / (double) totProb));
    }
  }


  // Test whether the given string is found within 
  // a fuzzy set in the list

  public boolean testWord (String s) {
    return name.equals("*") || name.equalsIgnoreCase(s);
  }
} 
