// QuestSet.java 98/03/23 1.0 
// Copyright (C) 1998 Robby Glen Garner and Paco Xander Nathan

package com.eagle;

import java.util.Vector;
import java.io.*;
import java.net.*;

public class QuestSet {
  public Vector imp = null;

  public QuestSet() {
    imp = new Vector(1000);
  }


  public boolean loadFile(String filename) {
    try {
      Quest r = null;
      String s;
      DataInputStream in = null;

      if (filename.startsWith("http:")) {

        URL url = null;

        try {
             url = new URL(filename);
            
           } catch (ArrayIndexOutOfBoundsException e) {
             System.err.println("wrong usage:");
           
           } catch (Exception e) {
                  System.err.println( e.toString() );
         }
         in = new DataInputStream(url.openStream());
      } else {

               // open the file as a stream
               File f = new File(filename);
               in = new DataInputStream(new FileInputStream(f));
      }


      // read each line
      while ((s = in.readLine()) != null) {

              // create some kind of Quest within the Questset
              if (s.startsWith("query:")) {
                 String name = s.substring(6);

                 r = new Quest();
	         r.addName(name.trim());
                 imp.addElement(r);
	      }
              else if (s.startsWith("target:")) {
                    r.addQuery(s.substring(7).trim());
                   }
                   else if (s.startsWith("signal:")) {
                      r.addSignal(s.substring(7).trim());
                   }
      }       


      System.out.println (imp.size() + " Quests Loaded.");

      // close the file
      in.close();
      return true;
    }
    catch (IOException e) {
      e.printStackTrace();
      return false;
    } 
  }


  public void DumpLog() {
    Quest r = null;

    // print a log of the query Quests
    for (int i = 0; i < imp.size(); i++) {
      r = (Quest) imp.elementAt(i);
      //System.out.println(r.name);
      //System.out.println(r.width);

      for (int j = 0; j < r.query.size(); j++) {
        System.out.println(r.query.elementAt(j));
        System.out.println(r.signal.elementAt(j));
      }
    }
  }

  public Quest findQuest(String qname) {

    boolean found = false;
    Quest r = null;

    for (int i = 0; (i < imp.size()) && (!found); i++) {
      r = (Quest) imp.elementAt(i);

      for (int j = 1; (j < r.width) && (!found); j++) {
               if (r.name.equalsIgnoreCase(qname)) {
	          found = true;
	       }
      }
    }

    return r;
  }
}
