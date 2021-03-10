// State (C) 2002 Robby Glen Garner 

import java.util.Enumeration;
import java.util.Vector;

public class State {
  public String name;
  public String value;

  public State() {
  }

  public void addName(String s) {
    name = s;
  }

  public void addValue(String s) {
    value = s;
  }

  public String getName() {
	  return name.toString();
  }

  public String getValue() {
	  return value.toString();
  }
}
