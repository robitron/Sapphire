/**
 * ExprEval: A class to evaluate arithmetic expressions.
 *
 * @author werner.hett@hta-bi.bfh.ch     27-Apr-1997
 * Version 2: ExprEvalException
 */

import java.util.*;
import java.io.*;

/**
 * ExprEval is a class that combines a variable pool with
 * methods to evaluate arithmetic expressions.
 */
public class ExprEval {

  private Hashtable var = new Hashtable();  // the variable pool
  private StreamTokenizer tokenizer;

  ExprEval () {
    clear();
  }

  /**
   * Evaluate a string representing an arithmetic expression
   * and return its numerical value.
   */
  public double evaluate (String exprString) throws ExprEvalException {
    StringBufferInputStream inStream = 
      new StringBufferInputStream (exprString);
    tokenizer = new StreamTokenizer (inStream);
    tokenizer.ordinaryChar ('-');
    tokenizer.ordinaryChar ('/');
    getToken();          // get the first token
    double value = expression();
    if (tokenizer.ttype == StreamTokenizer.TT_EOF)
      return value;
    else
      throw new ExprEvalException ("End of line expected; found "+token());
  }

  // process an arithmetic expression
  double expression ()  throws ExprEvalException {
    double value = sign();
    value *= term();
    while ((char) tokenizer.ttype == '+' || 
	   (char) tokenizer.ttype == '-') {
      if ((char) tokenizer.ttype == '+') {
	getToken();      // consume the + operator
	value += term();
      } else {
	getToken();      // consume the - operator
	value -= term();
      }
    }
    return value;
  }

  // process an optional unary sign (+ or -)
  double sign () {      
    if ((char) tokenizer.ttype == '+') {
      getToken();       // consume the + sign
      return 1;
    } else if ((char) tokenizer.ttype == '-') {
      getToken();      // consume the - sign
      return -1;
    } else {
      return 1;
    }
  }

  // process a term
  double term () throws ExprEvalException {
    double value = factor();
    while ((char) tokenizer.ttype == '*' || 
	   (char) tokenizer.ttype == '/') {
      if ((char) tokenizer.ttype == '*') {
	getToken();    // consume the * operator
	value *= factor();
      } else {
	getToken();   // consume the / operator
	value /= factor();
      }
    }
    return value;
  }

  // process a factor
  double factor () throws ExprEvalException {
    double value = 0;
    if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
      value = number();
    } else if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
      String name = tokenizer.sval;
      getToken();   // consume variable or function name
      if (tokenizer.ttype == '(') {
	getToken();   // consume left parenthesis
	// it's a function
	value = function (name);
      } else {
	// it's a variable (perhaps)
	value = recall (name);
      }
    } else if ((char) tokenizer.ttype == '(') {
      getToken();   // consume the left parenthesis
      value = expression();
      if ((char) tokenizer.ttype == ')') {
	getToken();   // consume the right parenthesis
      } else {
	throw new ExprEvalException(") expected; found "+token());
      }
    } else {
      throw new ExprEvalException("Factor expected; found "+token());
    }
    return value;
  }

  // process a number
  // The class java.io.StreamTokenizer recognizes real numbers
  // in the form dddd.ddd, but it ignores decimal exponents.
  // As a result of this (bug) we have to parse a posssible
  // exponent ourselves. This is done here.
  double number () throws ExprEvalException {
    double value = tokenizer.nval;
    getToken();      // consume the number (without exponent)
    if (tokenizer.ttype == StreamTokenizer.TT_WORD &&
	(tokenizer.sval.equals("E") || tokenizer.sval.equals("e"))) {
      getToken();    // consume the E or e
      int expoSign = (int) sign();
      if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
	int expo = expoSign * (int) tokenizer.nval;
	getToken();  // consume the exponent
        try {        // do the conversion
	  value = Double.valueOf(value+"e"+expo).doubleValue();
        }
        catch (NumberFormatException e) {
          throw new ExprEvalException("Wrong exponent format; found "+token());
        }
      } else {
        throw new ExprEvalException("Exponent expected; found "+token());
      }
    }
    return value;
  }

  // process a function call
  double function (String name) throws ExprEvalException {
    if (name.equals("abs")) {
      double a = param();
      return Math.abs (a);
    } else if (name.equals("acos")) {
      double a = param();
      return Math.acos (a);
    } else if (name.equals("asin")) {
      double a = param();
      return Math.asin (a);
    } else if (name.equals("atan")) {
      double a = param();
      return Math.atan (a);
    } else if (name.equals("atan2")) {
      double a = param0();
      double b = param();
      return Math.atan2 (a, b);
    } else if (name.equals("ceil")) {
      double a = param();
      return Math.ceil (a);
    } else if (name.equals("cos")) {
      double a = param();
      return Math.cos (a);
    } else if (name.equals("exp")) {
      double a = param();
      return Math.exp (a);
    } else if (name.equals("floor")) {
      double a = param();
      return Math.floor (a);
    } else if (name.equals("IEEEremainder")) {
      double f1 = param0();
      double f2 = param();
      return Math.IEEEremainder (f1, f2);
    } else if (name.equals("log")) {
      double a = param();
      return Math.log (a);
    } else if (name.equals("max")) {
      double a = param0();
      double b = param();
      return Math.max (a, b);
    } else if (name.equals("min")) {
      double a = param0();
      double b = param();
      return Math.min (a, b);
    } else if (name.equals("pow")) {
      double a = param0();
      double b = param();
      return Math.pow (a, b);
    } else if (name.equals("random")) {
      return Math.random ();
    } else if (name.equals("rint")) {
      double a = param();
      return Math.rint (a);
    } else if (name.equals("sin")) {
      double a = param();
      return Math.sin (a);
    } else if (name.equals("sqrt")) {
      double a = param();
      return Math.sqrt (a);
    } else if (name.equals("tan")) {
      double a = param();
      return Math.tan (a);
    } else
      throw new ExprEvalException("Unknown function "+name);
  }         

  // process a parameter with a trailing comma
  double param0 () throws ExprEvalException {
    double value = expression();
    if ((char) tokenizer.ttype == ',') {
      getToken();   //consume comma
      return value;
    } else
      throw new ExprEvalException("Comma expected; found "+token());
  }

  // process a parameter with a trailing right parenthesis
  double param ()  throws ExprEvalException {
    double value = expression();
    if ((char) tokenizer.ttype == ')') {
      getToken();   //consume right parenthesis
      return value;
    } else
      throw new ExprEvalException(") expected; found "+token());
  }

  // construct a string representation of the actual token
  String token() {
    if (tokenizer.ttype == StreamTokenizer.TT_EOF) 
      return "end of input";
    else if (tokenizer.ttype == StreamTokenizer.TT_WORD)
      return tokenizer.sval;
    else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER)
      return String.valueOf (tokenizer.nval);
    else return String.valueOf ((char) tokenizer.ttype);
  }

  // consume the current token and get the next one
  void getToken () {
    try {
      tokenizer.nextToken();
    } catch (IOException ioe) {
      System.out.println (ioe.toString());
    }
  }

  /**
   * Store a value under a name in the variable pool.
   */
  public void store (String name, double value) {
    var.remove (name);
    var.put (name, new Double (value));
  }

  /**
   * Return the value of a previously stored variable.
   */
  public double recall (String name)  throws ExprEvalException {
    Double result = (Double) var.get(name);
    if (result != null)
      return result.doubleValue();
    else
      throw new ExprEvalException("Unknown variable "+name);
  }

  /**
   * Clear the variable pool. Only the two mathematical constants
   * Math.E and Math.PI remain in the pool.
   */
  public void clear () {
    var.clear();
    store ("E", Math.E);
    store ("PI", Math.PI);
  }

  /**
   * Construct a string that represents the contents of the variable pool.
   */
  public String toString () {
    StringBuffer result = new StringBuffer();
    Enumeration keys = var.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      Double value = (Double) var.get(key);
      result.append (key);
      result.append (" = ");
      result.append (value);
      result.append ("\n");
    }
    return new String (result);
  }
}






