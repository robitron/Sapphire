// @(#)RegexRule.java 97/12/08 1.7
// Copyright (C) 1997-1999 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import com.oroinc.text.regex.*;
import java.util.*;

public class RegexRule extends Rule {
  class RegexPattern extends Object {
    Pattern pat = null;
    Vector var = null;

    RegexPattern (Pattern p, Vector v) {
      pat = p;
      var = v;
    }
  }


  public RegexRule (String name) {
    super(name);
  }


  // Add a regular expression as a rule element

  public void addElement (Phrase p, Perl5Compiler compiler) {
    StringBuffer buf = new StringBuffer();
    Vector v = new Vector();
    Pattern pat = null;

    // scan for regular expression pattern elements
    for (int i = 0; i < p.size(); i++) {
      String word = (String) p.elementAt(i);
      
      // save as a variable to bind found data
      if (word.startsWith("$")) {
	buf.append("(.+)");
	v.addElement(word.substring(1));
      }

      // append as a literal within the search pattern
      else
	buf.append(word);
    }

    // create the new regular expression to use searching later
    try {
      pat = compiler.compile(buf.toString(), Perl5Compiler.CASE_INSENSITIVE_MASK);
      addElement(new RegexPattern(pat, v));
    }
    catch (MalformedPatternException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }


  // Test for this rules patterns within the given stimulus

  public boolean testPhrase (String s, Properties p, PatternMatcher m, Grammar g) {
    for (int i = 0; i < size(); i++ ) {
      RegexPattern pat = (RegexPattern) elementAt(i);

      // check for a match with this element
      if (m.contains(s, pat.pat)) {
	MatchResult result = m.getMatch();

	// bind the variables as properties
	for (int j = 0; j < pat.var.size(); j++) {
	  String var = (String) pat.var.elementAt(j);
	  Phrase clause = new Phrase(result.group(j + 1).trim(), g.breaks);
	  g.removeFluff(clause);
	  p.put(var, clause.rePhrase(g, p).trim());
	}

	return true;
      }
    }

    return false;
  }


  // Lookup the indicated action rule by name and 
  // return the pointer to it

  public Vector lookup (Hashtable h) {
    Vector v = new Vector();
    String inv = getProperty(Rule.PROP_INVOKES);

    if (inv != null) {
      TSVector tsv = new TSVector(inv, " ", false, false);

      for (int i = 0; i < tsv.size(); i++) {
	String key = tsv.pop();
	ActionRule r = (ActionRule) h.get(key);
	v.addElement(r);
      }
    }

    return v;
  }
}
