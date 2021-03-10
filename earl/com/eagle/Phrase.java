// @(#)Phrase.java 09/06/29 1.9 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.util.*;

public class Phrase extends Vector {
  public int bindAt = 0;
  public String stimulus;
  public String expanded;


  // Tolkienize all the words in the input stimulus, based on the
  // given grammar

  public Phrase (String stim, String breaks) {
    stimulus = stim;
    StringTokenizer st = new StringTokenizer(stim, breaks, true);

    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      addElement(token);
    }
  }


  // Save a detokenized version of the input stimulus

  public void stashExpand () {
    StringBuffer buf = new StringBuffer(stimulus.length());

    for (int i = 0; i < size(); i++) {
      String word = (String) elementAt(i);

      if (!word.equals("")) {
	buf.append(word).append(" ");
      }
    }

    expanded = buf.toString().trim();
  }


  // Test the given fuzzy rule to see if this phrase matches its
  // criteria

  public boolean testRule (FuzzyRule r) {
    for (int i = 0; i < size(); i++) {
      if (r.testWord((String) elementAt(i))) {
	return true;
      }
    }

    return false;
  }


  // Test the given response rule to see if this phrase matches its
  // criteria

  public boolean testRule (ResponseRule r) {
    String stimulusLC = stimulus.toLowerCase();
    for (int i = 0; i < r.size(); i++) {
      String s = (String) r.elementAt(i);

      if (stimulusLC.indexOf(s) >= 0) {
	return true;
      }
    }

    return false;
  }


  // Bind the given action rule to some keyword in the input phrase

  public void bindRule (ActionRule r) {
    if (r.hasProperty(Rule.PROP_BIND)) {
      String anchor = r.getProperty(Rule.PROP_BIND);

      for (int i = 0; i < size(); i++) {
	if (anchor.equalsIgnoreCase((String) elementAt(i))) {
	  bindAt = i + 1;
	  return;
	}
      }
    }
  }


  // Map the person/tense of the response, starting at the binding
  // point and based on the given grammar

  public String rePhrase (Grammar g, Properties p) {
    StringBuffer buf = new StringBuffer(200);

    for (int i = bindAt; i < size(); i++) {
      String word = g.mapTense((String) elementAt(i));

      if (word.equalsIgnoreCase(g.lookup("I"))) {
	word = g.lookup("I");
      }

      if (!word.equals("")) {
	buf.append(" ").append(word);
      }
    }

    return buf.toString();
  }
}

