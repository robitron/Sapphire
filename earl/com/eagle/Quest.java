// Quest(ion) (C) 1998 Robby Glen Garner and Paco Xander Nathan

package com.eagle;

import java.util.Enumeration;
import java.util.Vector;

public class Quest {
  public String name;
  public int width;
  public Vector signal;
  public Vector query;


  public Quest() {
    query = new Vector(10, 10);
    signal = new Vector(10, 10);
    width = 0;
  }


  public void addName(String s) {
    name = s;
  }


  public void addQuery(String s) {
    query.addElement(s);
    width++;
  }

  public void addSignal(String s) {
    signal.addElement(s);
  }

  public String getQuery(int pos) {
    return (String) query.elementAt(pos);
  }

  public String getSignal(int pos) {
    return (String) signal.elementAt(pos);
  }

}
