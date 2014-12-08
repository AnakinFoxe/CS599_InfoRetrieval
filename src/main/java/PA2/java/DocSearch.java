package PA2.java;

/** Copyright 2014 smanna@csupomona.edu
 * !!WARNING!! STUDENT SHOULD NOT MODIFY THIS FILE.
 * NOTE THAT THIS FILE WILL NOT BE SUBMITTED, WHICH MEANS MODIFYING THIS FILE
 * WILL NOT TAKE EFFECT WHILE EVALUATING YOUR CODE.
 **/

import java.util.HashSet;
import java.util.Vector;

public interface DocSearch {
  public static enum SearchType {OR, AND}
  public void index(String dir) throws Exception;
  public HashSet<String> retrieve(Vector<String> query, SearchType op) throws Exception;  
}
