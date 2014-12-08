package PA3.java;

/** Copyright 2014 smanna@csupomona.edu
 * !!WARNING!! STUDENT SHOULD NOT MODIFY THIS FILE.
 * NOTE THAT THIS FILE WILL NOT BE SUBMITTED, WHICH MEANS MODIFYING THIS FILE
 * WILL NOT TAKE EFFECT WHILE EVALUATING YOUR CODE.
 **/

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

public class ProcessFile2 {

  private static final long MEGABYTE = 1024L * 1024L;

  public static double bytesToMegabytes(long bytes) {
    return 1.0 * bytes / MEGABYTE;
  }

  // getting query from a line
  public static Vector<String> getQueryFromLine(String qline) {
    Vector<String> query = new Vector<String>();
    String[] splittedStr = qline.split(" ");
    // subscript 0 to end of splittedStr contains the query
    for (int i = 0; i < splittedStr.length; ++i) {
      query.add(splittedStr[i]);
    }
    return query;
  }

  // geting result from a line
  public static Vector<String> getResultFromLine(String rline) {
    Vector<String> results = new Vector<String>();
    String[] splittedStr = rline.split(" ");
    // subscript 0 to end of splittedStr contains the query
    for (String str: splittedStr) {
      results.add(str);
    }
    return results;
  }

  // computing score
  public static double computeScore(
      Vector<String> allResults, Vector<String> expectedResults) {
    System.out.println("Expected results: " + expectedResults);
    System.out.println("All results: " + allResults);
    int numResultConsidered = Math.min(expectedResults.size(), 20);
    HashMap<String, Integer> ranksFound = new HashMap<String, Integer>();
    for (int i = 0; i < allResults.size() && i < numResultConsidered; ++i) {
      ranksFound.put(allResults.get(i), i);
    }
    HashMap<String, Integer> ranksExpected = new HashMap<String, Integer>();
    for (int i = 0; i < expectedResults.size() &&
        i < numResultConsidered; ++i) {
      ranksExpected.put(expectedResults.get(i), i);
        }
    int diff = 0, missing = 0, added = 0;
    for (String key : ranksFound.keySet()) {
      if (ranksExpected.containsKey(key)) {
        // Found in both
        diff = diff + Math.abs(ranksExpected.get(key) -
            ranksFound.get(key));
      } else {
        // New added
        ++added;
      }
    }
    for (String key : ranksExpected.keySet()) {
      if (!ranksFound.containsKey(key)) {
        // Penalize as missing
        ++missing;
      }
    }
    // Scoring Scheme:
    // 1. 0.5 for diff between ranks.
    //   a. diff between ranks will be normalized by number of iterms
    //     returned.
    //   b. No result returned will get full 0.5 here, as (2) will take care
    //     of penalizing.
    //   c. Max diversion would produce sum(diff) = numResultConsidered *
    //     (numResultConsidered - 1)
    double max_diff = numResultConsidered * (numResultConsidered - 1);
    if (max_diff == 0.0) {
      max_diff = 1.0;
    }
    double diff_score = 0.5 - (diff / max_diff);
    // 2. 0.5 for extra or missing entries.
    //   a. Highest diversion would be complete new set, which is missing =
    //     numResultConsidered & added = numResultConsidered.
    double added_missing_score= 0.5 - (0.5 * (added + missing) /
        numResultConsidered);
    double score = diff_score + added_missing_score;
    System.out.println("My score: diff=" + diff + " added=" + added +
        " missing=" + missing + " S=" + score);
    return score;
  }

  public static void main(String[] args) throws Exception {
    // begin
    String golden_file, docsdir;
    if (args.length == 0 || args[0].equals("test")) {
      golden_file = "src/main/java/PA3/data/test.txt";
      docsdir = "src/main/java/PA3/data/test";
    } else if (args[0].equals("eval")) {
      golden_file = "data/eval.txt";
      docsdir = "data/eval";
    } else {
      System.out.println("Unknown option: " + args[0]);
      System.exit(1);
      return;
    }
    System.out.println("=== Indexing... ===");
    RankedRetrievalModel2 rm = new RankedRetrievalModel2();
    rm.index(docsdir);
    // Get the Java runtime
    Runtime runtime = Runtime.getRuntime();
    // Run the garbage collector
    runtime.gc();
    // Calculate the used memory
    long indexSize = runtime.totalMemory() - runtime.freeMemory();
    double total_score = 0;
    long start_time_msec = System.currentTimeMillis();

    System.out.println("=== Searching... ===");
    File infile = new File(golden_file);
    Scanner sc = new Scanner(infile);
    // for over all lines of infile
    while (sc.hasNext()) {
      String str = sc.nextLine();
      String[] tabDel = str.split("\t");
      String qline = tabDel[0];
      String rline = tabDel[1];
      Vector<String> query = getQueryFromLine(qline);
      try {
        Vector<String> result = rm.retrieve(query);
        Vector<String> golden_result = getResultFromLine(rline);
        total_score += computeScore(result, golden_result);
      } catch (Exception e) {
        total_score -= 1;
      }
    }
    long end_time_msec = System.currentTimeMillis();
    System.out.println("Score: " + total_score + " Total Time (msec): " +
        (end_time_msec - start_time_msec));
    System.out.println("Used memory is bytes: " + indexSize);
    System.out.println("Used memory is megabytes: " + bytesToMegabytes(indexSize));
  }
}
