// @(#)Grammar.java 09/07/28 1.7 (C) 1997-1999 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.util.*;

public class Grammar extends Object {

  public static String breaks = "\t ~`'!&*()+={}[]|:;<,>.?/";

  public final static String[] contraIn = { "m", "d", "s", "ll", "re", "ve" };
  public final static String[] contraOut = { "am", "would", "is", "will", "are", "have" };

  public final static String[] negateIn = { "can", "don", "didn", "isn", "aren", "won", "shan", "couldn", "wouldn", "shouldn", "haven", "doesn" };
  static String[] negateOut = { "can", "do", "did", "is", "are", "will", "shall", "could", "would", "should", "have", "does" };

  public final static String[] tenseIn = { "you", "i", "me", "am", "are", "my", "your", "yourself", "myself", "my", "yours", "us" };
  public final static String[] tenseOut = { "I", "you", "you", "are", "am", "your", "my", "myself", "yourself", "yours", "mine", "y'all" };


  public String lookup (String s) {
    if (s.equals("I"))
      return "I";
    else
      return "";
  }


  public void removeCants (Vector v) {
    
    for (int i = 0; i < v.size(); i++) {
      String token = (String) v.elementAt(i);
      
      // overall exceptions

      if (token.equalsIgnoreCase("cannot")) {
	v.setElementAt("can", i);
	v.insertElementAt("not", i + 1);
      }

      else if ((i > 0) &&
	       (token.charAt(0) == '\'') &&
	       (i < (v.size() - 1))
		) {
	String nextToke = (String) v.elementAt(i + 1);
	String prevToke = (String) v.elementAt(i - 1);
	boolean found = false;
	
	// exceptions to contraction rules

	if (prevToke.equalsIgnoreCase("let") &&
	    nextToke.equalsIgnoreCase("s")
	     ) {
	  v.setElementAt("us", i + 1);
	  v.removeElementAt(i);
	  found = true;
	}

	// listed contractions

	else {
	  for (int j = 0; j < contraIn.length; j++) {
	    if (nextToke.equalsIgnoreCase(contraIn[j])) {
	      v.setElementAt(contraOut[j], i + 1);
	      v.removeElementAt(i);
	      found = true;
	    }
	  }
	}
	
	if (!found) {

	  // omitted endings

	  if (prevToke.endsWith("in")) {
	    v.setElementAt(prevToke + "g", i - 1);
	    v.removeElementAt(i + 1);
	    v.removeElementAt(i);
	  }
	  
	  // Southern proper speech

	  else if (prevToke.equalsIgnoreCase("y") &&
		   nextToke.equalsIgnoreCase("all")
		    ) {
	    v.setElementAt("you", i - 1);
	    v.removeElementAt(i);
	  }

	  // negations

	  else if (nextToke.equalsIgnoreCase("t")) {
	    for (int j = 0; j < negateIn.length; j++) {
	      if (prevToke.equalsIgnoreCase(negateIn[j])) {
		v.setElementAt(negateOut[j], i - 1);
		v.setElementAt("not", i + 1);
		v.removeElementAt(i);
	      }
	    }
	  }
	}
      }
    }
  }


  public void removeFluff (Vector v) {
    for (int i = 0; i < v.size(); i++) {
      String token = (String) v.elementAt(i);

      if (token.equals("&"))
	v.setElementAt("and", i);
      else if (breaks.indexOf(token.charAt(0)) >= 0)
	v.removeElementAt(i);
    }
  }


  public String mapTense (String word) {
    boolean found = false;
    String result = word;

    for (int i = 0; (i < tenseIn.length) && (!found); i++) {
      if (word.equalsIgnoreCase(tenseIn[i])) {
	result = tenseOut[i];
	found = true;
      }
    }

    return result;
  }
}

