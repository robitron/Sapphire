// EARL.java 09/10/08 2.35 Robby Glen Garner and Paco Xander Nathan

package com.eagle;
import com.oroinc.text.regex.*;
import java.util.*;
import java.io.*;
import java.net.*;

public class EARL extends Object {
  private final static boolean debug = false;

  public static Random rand = new Random();
  public static Config config = new Config(new File(System.getProperty("user.dir"), "jfred.cfg"));

  protected boolean isVerbose = true;
  protected Grammar grammar = null;
  protected ActionRule intro = null;

  protected Vector vResp = new Vector();
  protected ResponseRule resp[] = null;
  public Hashtable respHash = new Hashtable();

  protected Vector vRegx = new Vector();
  protected RegexRule regx[] = null;

  protected Vector vFuzz = new Vector();
  protected FuzzyRule fuzz[] = null;
  public Hashtable fuzzHash = new Hashtable();

  protected Vector vActs = new Vector();
  protected ActionRule acts[] = null;
  public Hashtable actsHash = new Hashtable();

  public String RuleSetName = "jfred.dat";

  // define the regex pattern matcher

  public static Perl5Compiler compiler = new Perl5Compiler();
  public static PatternMatcher matcher = new Perl5Matcher();
  public static Pattern patProp = null;
  public static Pattern patFuzz = null;

  static {
    try {
      patProp = compiler.compile("^\\s+(\\S+):\\s+(\\S+.*)$", Perl5Compiler.CASE_INSENSITIVE_MASK);
      patFuzz = compiler.compile("^\\s+(\\S+)\\s+(\\S+.*)$", Perl5Compiler.CASE_INSENSITIVE_MASK);
    }
    catch (MalformedPatternException e) {
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }


  // Constructors

  public EARL () {
    setupDefaults();
  }

  //public EARL (File f, Grammar grammar, boolean isVerbose) {
  //  this.grammar = grammar;
  //  this.isVerbose = isVerbose;
  //  setupDefaults();
  //
  //  if (f.exists()) {
  //    loadFile(f);
  //    prepRules();
  //  }
  //}

  public EARL (Grammar grammar, boolean isVerbose, String sRulez) {
    RuleSetName = sRulez;
    this.grammar = grammar;
    this.isVerbose = isVerbose;
    setupDefaults();
    loadFile(sRulez);
    prepRules();
  }

//  public EARL (String RuleSetName, Grammar grammar, boolean isVerbose) {
//    this.grammar = grammar;
//    this.isVerbose = isVerbose;
//    setupDefaults();
//    loadFile(RuleSetName);
//    prepRules();
//  }


  // Setup the default properties of the configuration

  protected void setupDefaults () {
    config.setDefault("log.max", "200");
    config.setDefault("intro.rate", "97");
    config.setDefault("rulestack.max", "50000");
  }


  // Keep track of the grammar to be used

  public void setGrammar (Grammar grammar, boolean isVerbose) {
    this.grammar = grammar;
    this.isVerbose = isVerbose;
  }

  public Grammar getGrammar () {
    return grammar;
  }

  public void setVerbose (boolean isVerbose) {
    this.isVerbose = isVerbose;
  }

  public boolean getVerbose () {
    return isVerbose;
  }


  // Prepare the rules database for action

  public void prepRules () {
    vResp.copyInto(resp = new ResponseRule[vResp.size()]);
    vResp = null;

    vRegx.copyInto(regx = new RegexRule[vRegx.size()]);
    vRegx = null;

    // normalize each of the fuzzy rules

    vFuzz.copyInto(fuzz = new FuzzyRule[vFuzz.size()]);
    vFuzz = null;

    for (int i = 0; i < fuzz.length; i++) {
      fuzz[i].normalize(actsHash);
    }

    // randomly shuffle the order of responses within all the action
    // rules, and establish priority rankings

    vActs.copyInto(acts = new ActionRule[vActs.size()]);
    vActs = null;

    for (int i = 0; i < acts.length; i++) {
      acts[i].shuffle();
      acts[i].setPriority();
    }

    // randomly shuffle the intro rule(s)

    if (intro != null) {
      intro.shuffle();
    }
  }


  // Load the rule database from a file

  public boolean loadFile (String RuleSetName) {
  DataInputStream in = null;


              File f = new File("./");
              System.out.println("\nLoading.\n");

              FilenameFilter textFilter = new FilenameFilter() {
                  public boolean accept(File dir, String name) {
                      return name.toLowerCase().endsWith(".erl");
                  }
              };

              File[] files = f.listFiles(textFilter);
              for (File file : files) {
                  if (file.isFile()) {

                      System.out.print("     Module:");
                  }
                  System.out.println(file.getName());
              }


    try {

      if (RuleSetName.startsWith("http:")) {
          URL url = null;

          try {
                url = new URL(RuleSetName);

             } catch (ArrayIndexOutOfBoundsException e) {
               System.err.println("wrong usage:");
             }

          // open the file as a stream
          in = new DataInputStream(url.openStream());
      }
      else {
             in = new DataInputStream(new FileInputStream(new File(RuleSetName)));
           }


      Rule r = null;
      String s;

      //System.out.print("\nloading: " + RuleSetName);

      // read each line

      while ((s = in.readLine()) != null) {
	if (s.trim().equals("") || s.trim().startsWith("#")) {
	  // ignore blank lines
	}

	else if (s.charAt(0) != '\t') {

	  // create an introduction

	  if (s.toLowerCase().startsWith(Rule.TYPE_INTRO)) {
	    r = new ActionRule(s.substring(Rule.TYPE_INTRO.length()).trim());

	    if (intro != null) {
	      System.out.println("redefining intro rule: " + r.getName());
	    }

	    intro = (ActionRule) r;
	  }

	  // create a response qualifier

	  else if (s.toLowerCase().startsWith(Rule.TYPE_RESPONSE)) {
	    String name = s.substring(Rule.TYPE_RESPONSE.length()).trim();
	    vResp.addElement(r = new ResponseRule(name));
	    respHash.put(r.getName(), r);
	  }

	  // create a regular expression

	  else if (s.toLowerCase().startsWith(Rule.TYPE_REGEX)) {
	    String name = s.substring(Rule.TYPE_REGEX.length()).trim();
	    vRegx.addElement(r = new RegexRule(name));
	  }

	  // create an action rule

	  else if (s.toLowerCase().startsWith(Rule.TYPE_ACTION)) {
	    String name = s.substring(Rule.TYPE_ACTION.length()).trim();
	    r = (ActionRule) actsHash.get(name);

	    if (r != null) {
	      System.out.println("redefining action rule: " + r.getName());
	    }

	    vActs.addElement(r = new ActionRule(name));
	    actsHash.put(r.getName(), r);
	  }

	  // create a fuzzy set

	  else if (s.toLowerCase().startsWith(Rule.TYPE_FUZZY)) {
	    String name = s.substring(Rule.TYPE_FUZZY.length()).trim();
	    r = (FuzzyRule) fuzzHash.get(name);

	    if (r == null) {
	      vFuzz.addElement(r = new FuzzyRule(name));
	      fuzzHash.put(r.getName(), r);
	    }
	  }
	}

	// either use this line as a property, or add another action
	// to the current rule

	else if (!testProperty(s, r)) {
	  if (r instanceof ResponseRule) {
	      r.addElement(s.trim().toLowerCase());
	  }

	  else if (r instanceof RegexRule) {
	    RegexRule rr = (RegexRule) r;
	    rr.addElement(new Phrase(s.trim(), grammar.breaks), compiler);
	  }

	  else if (r instanceof FuzzyRule) {
	    FuzzyRule fr = (FuzzyRule) r;

	    if (matcher.contains(s, patFuzz)) {
	      MatchResult result = matcher.getMatch();
	      String prob = result.group(1).trim();
	      String ref = result.group(2).trim();

	      try {
		fr.addElement(Integer.parseInt(prob), ref);
	      }
	      catch (NumberFormatException e) {
		System.err.println("EARL: fuzzy set probability not understood - " + prob);
		System.err.println(e.getMessage());
		e.printStackTrace();
	      }
	    }
	    else {
	      System.err.println("EARL: fuzzy set not understood - " + s);
	    }
	  }

	  else if (r instanceof ActionRule) {
	    r.addElement(s.trim());
	  }
	}
      }

      // close the file

      in.close();
      System.out.println("\n" + (vActs.size() + vResp.size() + vRegx.size() + vFuzz.size()) + " rules.\n");

      return true;
    }
    catch (IOException e) {
      System.err.println("EARL: I/O error - " + e.getMessage());
      e.printStackTrace();

      return false;
    }
  }


  // Test/parse whether the input line is a property
  // definition -- returns TRUE if found

  protected boolean testProperty (String s, Rule r) {
    if (matcher.contains(s, patProp)) {
      MatchResult result = matcher.getMatch();

      String name = result.group(1).toLowerCase().trim();
      String value = result.group(2).trim();

      if (debug) {
	System.out.println("EARL: prop name - " + name + ", value - " + value);
      }

      // check for URLs -- possible multiple ones listed per rule

      if (name.equals(Rule.PROP_URL) && (r instanceof ActionRule)) {
	if (debug) {
	  System.out.println("EARL: adding URL " + value + " to rule " + r.getName());
	}

	ActionRule ar = (ActionRule) r;
	ar.url.addElement(value);
      }

      // check for responses -- possible multiple ones listed per rule

      else if (name.equals(Rule.PROP_EXPECT) && (r instanceof ActionRule)) {
	if (debug) {
	  System.out.println("EARL: adding response " + value + " to rule " + r.getName());
	}

	ActionRule ar = (ActionRule) r;
	ar.expect.addElement(value);
      }

      // otherwise, just add the property

      else {
	r.putProperty(name, value);
      }

      return true;
    }

    return false;
  }


  // Select a rule for a target response template, based on key words
  // from the given stimulus phrase

  protected ActionRule chooseReply (Phrase p, Context c, boolean hasHTML) {
    FuzzyUnion fu = new FuzzyUnion();
    ActionRule defRule = (ActionRule) actsHash.get("TOPIC");
//     ActionRule loopRule = null;
    boolean beNormal = true;

    // look for expected responses

    if (c.expect.size() > 0) {
      Enumeration e = c.expect.elements();
      ActionRule prevRule = (ActionRule) actsHash.get((String) e.nextElement());
      //defRule = (ActionRule) actsHash.get("IRP");

      // test each potential response

      while (e.hasMoreElements()) {
	StringTokenizer st = new StringTokenizer((String) e.nextElement(), " ", false);
	ResponseRule rr = (ResponseRule) respHash.get(st.nextToken());

	if ((rr != null) && p.testRule(rr)) {

	  // we got an allowable response, so add all the predicated
	  // rules

	  while (st.hasMoreTokens()) {
	    ActionRule ar = (ActionRule) actsHash.get(st.nextToken());

	    if (ar != null) {
	      fu.addRule(ar);
	      beNormal = false;
	    }
	  }

	  // done searching... log the response and leave

	  String varName = prevRule.getProperty(Rule.PROP_SETVAR);

	  if (varName != null) {
	    c.dirt.put(varName, p.stimulus);
	  }

	  break;
	}
      }

      // if only the default "IRP" rule is included, at this point,
      // then they didn't answer the question properly...
      // NB: the following code will cause The Interrogator to loop
      // mercilessly!!

      /*
      if (fu.size() == 1) {
	loopRule = prevRule;
	loopRule.putProperty(Rule.PROP_REPEAT, "true");
      }
      */
    }

    // go with the rest of the stimulus/response conditions...

    if (beNormal) {

      // start out by adding the sequenced rules

      for (int i = 0; i < c.next.size(); i++) {
	ActionRule ar = (ActionRule) c.next.elementAt(i);
	fu.addRule(ar);
      }

      // make a list of all the fuzzy sets that match

      for (int i = 0; i < fuzz.length; i++) {
	if (p.testRule(fuzz[i])) {
	  fu.addRule(fuzz[i]);
	}
      }

      // add to that list all the regular expression rules which match

      for (int i = 0; i < regx.length; i++) {
	if (regx[i].testPhrase(p.expanded, c.dirt, matcher, grammar)) {
	  fu.addRules(regx[i].lookup(actsHash));
	  break;
	}
      }
    }

    // cull the list based on required properties, then sort for rule
    // fitness and prior use

    fu.cullProperties(c.dirt, hasHTML);
    // fu.sort();  // a sorted list is not required
    fu.priorUse(c.used);

    // print a trace of internal structures, if requested

    if (isVerbose) {
      fu.printTrace(c.used);
    }

    // select one rule as the response template, using the default
    // rule in case there is no preferable match

//     if (loopRule != null) {
//       defRule = loopRule;
//     }

    ActionRule ar = fu.selectOne(defRule);
    c.logRule(ar);

    return ar;
  }


  // Formulate a reply to the given phrase, using the given context

  public String formReply (Phrase p, Context c, boolean hasHTML) {
    StringBuffer reply = new StringBuffer(4096);
    ActionRule ar = null;
    String oldQuery = "";

    // save a copy of the current query, if any

    if (c.dirt.containsKey(Rule.PROP_TELLME)) {
      oldQuery = c.dirt.getProperty(Rule.PROP_TELLME);
    }

    // select an optional introduction

    if (intro != null) {
      if ((Math.abs(rand.nextInt()) % 100) > 97) {
	reply.append(intro.nextAction(p.stimulus, c.dirt)).append(" ");
      }
    }

    // select a target response template based on key words from the
    // input stream

    ar = chooseReply(p, c, hasHTML);
    System.out.println("Rule: " + ar.name);

    String action = ar.nextAction(p.stimulus, c.dirt);

    String glob = ar.getProperty(Rule.PROP_SETVAR);

    if (glob != null) {
       String dleft = glob.substring(0, glob.indexOf(" "));
       String dright = glob.substring(glob.indexOf(" ") + 1, glob.length());
       c.dirt.put(dleft, dright);  // p.stimulus
       System.out.println(dleft + " : " + dright);
    }

    // test for insertion points in the selected response template

    int insertAt = action.indexOf("[]");

    if (insertAt < 0) {
      reply.append(action);
    }

    // map the input verb tense and possessives/contractions...
    // NB: some kind of context-free grammar might work better here

    else {
      reply.append(action.substring(0, insertAt - 1));

      p.bindRule(ar);

      reply.append(p.rePhrase(grammar, c.dirt));
      reply.append(action.substring(insertAt + 2));
    }



    // decide whether the current query differs from the previous
    // one...

    if (c.dirt.containsKey(Rule.PROP_TELLME)) {
      String newQuery = c.dirt.getProperty(Rule.PROP_TELLME);

      if (!newQuery.equals(oldQuery)) {
	if (newQuery.toLowerCase().startsWith("about ")) {
	  newQuery = newQuery.substring(6).trim();
	}

	c.query = newQuery;
      }
    }

    // keep track of what's been said

    String response = reply.toString();
    c.logChat(p.stimulus, response);

    return response;
  }
}
