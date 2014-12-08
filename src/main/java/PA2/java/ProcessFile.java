package PA2.java;

/** Copyright 2014 smanna@csupomona.edu
 * !!WARNING!! STUDENT SHOULD NOT MODIFY THIS FILE.
 * NOTE THAT THIS FILE WILL NOT BE SUBMITTED, WHICH MEANS MODIFYING THIS FILE
 * WILL NOT TAKE EFFECT WHILE EVALUATING YOUR CODE.
 **/

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Vector;

public class ProcessFile {

  private static int errCount = 0;
  private static long totalFp = 0;

  private static final long MEGABYTE = 1024L * 1024L;

  public static double bytesToMegabytes(long bytes) {
    return 1.0 * bytes / MEGABYTE;
  }

  // getting query from a line
  public static Vector<String> getQueryFromLine(String qline) {
    Vector<String> query = new Vector<String>();
    String[] splittedStr = qline.split(" ");
    // subscript 1 to end of splittedStr contains the query
    for (int i = 1; i < splittedStr.length; ++i) {
      query.add(splittedStr[i]);
    }
    return query;
  }

  // getting search type from a line
  public static BooleanRetrievalModel.SearchType
  getSearchTypeFromLine(String qline) {
    String[] splittedStr = qline.split(" ");
    // subscript 0 of splittedStr contains the search type
    if (splittedStr[0].equalsIgnoreCase("or")) {
      return BooleanRetrievalModel.SearchType.OR;
    }
    else if (splittedStr[0].equalsIgnoreCase("and")) {
      return BooleanRetrievalModel.SearchType.AND;
    }
    else {
      System.out.println("Invalid search type!");
      return null;
    }
  }

  // geting result from a line
  public static HashSet<String> getResultFromLine(String rline) {
    HashSet<String> results = new HashSet<String>();
    String[] splittedStr = rline.split(" ");
    // subscript 0 to end of splittedStr contains the query
    for (String str: splittedStr) {
      results.add(str);
    }
    return results;
  }

  // computing score
  public static double computeScore(HashSet<String> allResults,
      HashSet<String> expectedResults,
      Vector<String> query,
      DocSearch.SearchType op) {
    try {
      long tp = 0, fp = 0, fn = 0;
      long score = 0;
      // Compare eact set
      for (String ret : allResults) {
        if (expectedResults.contains(ret)) {
          ++tp;
        } else {
          ++fp;
          ++totalFp;
        }
      }

      for (String ret : expectedResults) {
        if (!allResults.contains(ret)) {
          ++fn;
        }
      }
      score = (tp - fp - fn);
        if (fp != 0 || fn != 0) {
//            System.out.print(++errCount + ": q:" +query + " st:" + op);
//            System.out.println("    ==> fp:" + fp + " fn:" + fn);
        }
      return 1.0 * score / expectedResults.size();
    } catch (NullPointerException e) {
      System.out.println("Null Pointer");
      return -1;
    }
  }


  public static void main(String[] args) throws Exception {
    // begin
    String golden_file, docsdir;
    if (args.length == 0 || args[0].equals("test")) {
      golden_file = "src/PA2/data/test.txt";
      docsdir = "src/PA2/data/test";
    } else if (args[0].equals("eval")) {
      golden_file = "data/eval.txt";
      docsdir = "data/eval";
    } else {
      System.out.println("Unknown option: " + args[0]);
      System.exit(1);
      return;
    }
    System.out.println("Indexing...");
    BooleanRetrievalModel bm = new BooleanRetrievalModel();
    bm.index(docsdir);
    // Get the Java runtime
    Runtime runtime = Runtime.getRuntime();
    // Run the garbage collector
    runtime.gc();
    // Calculate the used memory
    long indexSize = runtime.totalMemory() - runtime.freeMemory();

    File infile = new File(golden_file);

    int total_run = 30;
    long total_time = 0;
    for (int idx = 0; idx < total_run; ++idx) {
        double total_score = 0;
        long start_time_msec = System.currentTimeMillis();

        Scanner sc = new Scanner(infile);
        // for over all lines of infile
        while (sc.hasNext()) {
            String str = sc.nextLine();
            String[] tabDel = str.split("\t");
            String qline = tabDel[0];
            String rline = tabDel[1];
            Vector<String> query = getQueryFromLine(qline);
            DocSearch.SearchType st = getSearchTypeFromLine(qline);
//      System.out.println("q:" +query + " st:" + st);
            try {
                HashSet<String> result = bm.retrieve(query, st);
                HashSet<String> golden_result = getResultFromLine(rline);
                total_score += computeScore(result, golden_result, query, st);
            } catch (Exception e) {
                total_score -= 1;
            }
        }
        long end_time_msec = System.currentTimeMillis();
        System.out.println("Score: " + total_score + " Total Time (msec): " +
                (end_time_msec - start_time_msec));

//        System.out.println("Total Num of Dic: " + bm.countIndex());
//        System.out.println("Total FP: " + totalFp);
//        System.out.println("Number of Loops: " + bm.getLoopCounter_());
        bm.setLoopCounter_(0);
        total_time += end_time_msec - start_time_msec;
    }
      System.out.println("Used memory is bytes: " + indexSize);
      System.out.println("Used memory is megabytes: " + bytesToMegabytes(indexSize));
      System.out.println("Average Time (msec): " + total_time / total_run);
  }
}
