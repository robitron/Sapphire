// @(#)Context.java 09/06/29 1.21 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import java.io.*;
import java.util.*;

public class Context extends Object {
  private final static boolean debug = false;

  public Stack stimLog = new Stack();
  public Stack respLog = new Stack();

  public RuleStack used = new RuleStack();
  public RuleStack next = new RuleStack();

  public Properties dirt = null;
  public Cart cart = null;
  public Vector url = null;
  public Vector expect = new Vector();

  public String query = "";
  public Hashtable actsHash = null;

  protected static String logHeader[] = {
    "handle", "hostname", "referral", "name", "firm",
    "address", "city", "province", "country", "postalcode",
    "tel", "fax", "email", "language",
    "timestamp", "hits", "user-agent", "accept-language",
  };

  protected static String jfdHeader[] = {
    "nickname", "haveyou", "doyou", "areyou", "tellme",
  };


  // Link to the given cart, then synchronize
  // the rule list with the given EARL object

  public Context (Cart c, EARL f) {
    dirt = new Properties(c);

    cart = c;
    actsHash = f.actsHash;
  }


  // Keep a trace of which rules have been used

  public void logRule (ActionRule r) {

    // if we've used this rule before, pull it out of the stack to
    // prevent cycles

    used.addRule(r);

    // if this rule was sequenced, likewise pull it out of the "next"
    // stack to prevent cycles

    if (next.hasRule(r)) {
      next.removeRule(r);
    }

    // look for sequencing to follow this rule

    if (r.hasProperty(Rule.PROP_NEXT)) {
      TSVector tsv = new TSVector(r.getProperty(Rule.PROP_NEXT), " ", false, false);

      for (int i = 0; i < tsv.size(); i++) {
	       String key = tsv.pop();
	       ActionRule seq = (ActionRule) actsHash.get(key);

	       next.addRule(seq);
      }
    }

    // gather the indicated URLs and expected responses

    url = (Vector) r.url.clone();
    expect = (Vector) r.expect.clone();

    if (expect.size() > 0) {
      if (debug) {
	       System.out.println("expecting: " + r.getName());
      }

      expect.insertElementAt(r.getName(), 0);
    }
  }


  // Keep track of what's been said

  public void logChat (String stimulus, String response) {
    stimLog.push(stimulus);
    respLog.push(response);
  }


  // Load a Stack from a text file

  protected void loadStack (BufferedReader f, Stack s, int max) {
    try {
      String lineIn = f.readLine();

      if (lineIn != null) {
	       s.removeAllElements();
	       int size = Integer.parseInt(lineIn);

	      for (int i = 0; i < size; i++) {
	          lineIn = f.readLine();

	           if (lineIn == null) {
	              return;
	             }

	           s.push(lineIn);
	      }

	// trim the stack depths

	for (int i = 0; i < (s.size() - max); i++) {
	  s.removeElementAt(0);
	}
      }
    }
    catch (IOException e) {
      return;
    }
  }


  // Load a Vector from a text file

  protected void loadVector (BufferedReader f, Vector v) {
    try {
      String lineIn = f.readLine();

      if (lineIn != null) {
	v.removeAllElements();
	int size = Integer.parseInt(lineIn);

	for (int i = 0; i < size; i++) {
	  lineIn = f.readLine();

	  if (lineIn == null) {
	    return;
	  }

	  v.addElement(lineIn);
	}
      }
    }
    catch (IOException e) {
      return;
    }
  }


  // Save a Vector to a text file

  protected void saveVector (PrintWriter out, Vector v) {
    out.println(v.size());

    for (int i = 0; i < v.size(); i++) {
      String line = (String) v.elementAt(i);
      out.println(line);
    }
  }


  // Read persistent file

  public void loadFile () {
    InputStream f = cart.getInputStream(Cart.CHAT_FILE);

   if (f != null) {
      try {
	           BufferedReader in = new BufferedReader(new InputStreamReader(f));

	            // load the stimulus/response logs

	           int max = 200;

	           loadStack(in, stimLog, max);
	           loadStack(in, respLog, max);

	            // load the expected responses

            loadVector(in, expect);

	          // load the rule firing logs

	          used.loadRules(in, actsHash);
	          next.loadRules(in, actsHash);

	          // load the actual dirt properties

	          dirt = new Properties(cart);
            dirt.load(f);

	          // close the stream
	          in.close();
      }
      catch (IOException e) {
	       return;
      }
    }
  }


  // Write persistent file

  public void saveFile () {
    OutputStream f = cart.getOutputStream(Cart.CHAT_FILE);

    if (f != null) {
      PrintWriter out = new PrintWriter(f, true);

      // save the stimulus/response logs

      saveVector(out, stimLog);
      saveVector(out, respLog);

      // save the expected responses

      saveVector(out, expect);

      // save the rule firing logs

      used.saveRules(out);
      next.saveRules(out);

      // save the actual dirt properties

      dirt.save(f, "dirt");

      // close the stream

      out.close();
    }
  }


  // Access the list of found URLs, if any

  public String[] getLinks () {
    String urlList[] = null;

    if ((url != null) && (url.size() > 0)) {
      url.copyInto(urlList = new String[url.size()]);
    }

    return urlList;
  }


  // Write a dump of this session out
  // to the given buffer

  public void dumpLog (StringBuffer buf) {

    // dump the cart address info

    for (int i = 0; i < logHeader.length; i++) {
      if (cart.containsKey(logHeader[i])) {
	buf.append(logHeader[i] + ": " + cart.getProperty(logHeader[i])).append("\n");
      }
    }

    // dump the dirt properties

    for (int i = 0; i < jfdHeader.length; i++) {
      if (dirt.containsKey(jfdHeader[i])) {
	       buf.append(jfdHeader[i] + ": " + dirt.getProperty(jfdHeader[i])).append("\n");
      }
    }

    buf.append("\n");

    // dump the stimulus/response exchanges

    for (int i = 0; i < stimLog.size(); i++) {
      buf.append("> " + (String) stimLog.elementAt(i)).append("\n");
      buf.append("< " + (String) respLog.elementAt(i)).append("\n");
    }

    buf.append("\n");
  }
}
