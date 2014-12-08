package PA2.java;

/** Copyright 2014: smanna@csupomona.edu
 *
 * You need to implement BooleanRetrievalModel using SkipList.
 * You should have everything within this file.
 */

import java.io.*;
import java.util.*;

public class BooleanRetrievalModel implements DocSearch {

    private final HashMap<String, HashSet<String>> index1_ = new HashMap<>();
    private final HashMap<String, ArrayList<Integer>> index2_ = new HashMap<>();
    private final HashMap<String, int[]> index3_ = new HashMap<>();

    private long loopCounter_ = 0;   // for debuging purpose

    // begin private class
    private class SkipList {

        // Node in skip-list
        private class Node {

        }

        // constructor
        public SkipList() {
        }

        public String toString() {
          // TODO(student)
          return null;
        }

        // TODO(student): Implement all other methods needed to implement index() &
        // retrieve() below.

    }

    // Constructor
    public BooleanRetrievalModel() {
    }

    public void setLoopCounter_(long loopCounter_) {
        this.loopCounter_ = loopCounter_;
    }

    public long getLoopCounter_() {
        return loopCounter_;
    }

    public long countIndex() throws IOException {
        long count = 0;
        FileWriter fw = new FileWriter("temp.txt");
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            for (String key : index3_.keySet()) {
                int[] filenames = index3_.get(key);
                String line = "" + key;
                for (Integer filename : filenames) {
                    ++count;
                    line += " " + filename.toString();
                }
                bw.write(line + "\n");
            }
        }

        return count;
    }

    public void index(String dir) throws Exception {
        File[] files = new File(dir).listFiles();

        // only for array conversion
        HashMap<String, ArrayList<Integer>> index = new HashMap<>();

        for (File file : files) {
            String filename = file.getName();

            if (filename.contains("DS_Store"))
                continue;

            FileReader fr = new FileReader(file);
            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    // to lowercase
                    line = line.toLowerCase();

                    // index
                    line = line.replaceAll("[^a-z]", " ");
                    String[] words = line.split(" ");
//                    indexWords1(words, filename);
//                    indexWords2(words, filename, index2_);
                    indexWords2(words, filename, index);
                }
            }
        }

        // convert to arrays
        for (String key : index.keySet()) {
            ArrayList<Integer> filenames = index.get(key);
            int[] filenamesArray = new int[filenames.size()];
            for (int idx = 0; idx < filenames.size(); ++idx)
                filenamesArray[idx] = filenames.get(idx);

            index3_.put(key, filenamesArray);
        }
    }

    public HashSet<String> retrieve(Vector<String> query,
                                  DocSearch.SearchType op) throws Exception {
//        HashSet<String> ret1 = retrieve1(query, op);
//        HashSet<String> ret2 = retrieve2(query, op);
//        HashSet<String> ret3 = retrieve3(query, op);
        HashSet<String> ret4 = retrieve4(query, op);


//        if (ret1.size() != ret2.size()) {
//            System.out.println("q:" +query + " st:" + op);
//            System.out.println(ret1);
//            System.out.println(ret2);
//        }

        return ret4;
    }

    // tokenize
    private Vector<String> tokenizeADoc(String infile) throws Exception {
    // TODO(student): Feel free to use tokenizer already implemented in PA1
    return null;
    }

    // retrieval with HashSet
    private HashSet<String> retrieve1(Vector<String> query,
                                      DocSearch.SearchType op) throws Exception {
        String firstKey = query.get(0).toLowerCase();
        HashSet<String> results = new HashSet<>(index1_.get(firstKey));

        for (int idx = 1; idx < query.size(); ++idx) {
            String key = query.get(idx).toLowerCase();
            switch (op) {
                case OR:
                    results.addAll(index1_.get(key));
                    break;
                case AND:
                    HashSet<String> temp = new HashSet<>();
                    for (String filename : results)
                        if (index1_.get(key).contains(filename))
                            temp.add(filename);
                    results = temp;
                    break;
                default:
                    break;
            }
        }

        return results;
    }

    // index the words using HashSet
    private void indexWords1(String[] words, String filename) {
        for (int idx = 0; idx < words.length; ++idx) {
            HashSet<String> filenames;
            if (index1_.containsKey(words[idx]))
                filenames = index1_.get(words[idx]);
            else
                filenames = new HashSet<>();
            filenames.add(filename);
            index1_.put(words[idx], filenames);
        }
    }

    // retrieval with ArrayList
    private HashSet<String> retrieve2(Vector<String> query,
                                      DocSearch.SearchType op) throws Exception {
        HashSet<String> results = new HashSet<>();

        switch (op) {
            case OR:
                for (int idx = 0; idx < query.size(); ++idx) {
                    String key = query.get(idx).toLowerCase();
                    ArrayList<Integer> tmp = index2_.get(key);
                    for (Integer filename : tmp)
                        results.add(filename.toString());
                }
                break;
            case AND:
                ArrayList<Integer> upperList = index2_.get(query.get(0));
                for (int idx = 1; idx < query.size(); ++idx) {
                    // get the key
                    String key = query.get(idx).toLowerCase();

                    // get the list
                    ArrayList<Integer> lowerList = index2_.get(key);
                    ArrayList<Integer> merge = new ArrayList<>();

                    int upper = 0;  // cursor for upperList
                    int lower = 0;  // cursor for lowerList

                    // merge
                    while ((upper < upperList.size()) && (lower < lowerList.size())) {
                        ++loopCounter_;

                        if (upperList.get(upper) > lowerList.get(lower))
                            ++lower;
                        else if (upperList.get(upper) < lowerList.get(lower))
                            ++upper;
                        else {
                            merge.add(upperList.get(upper));
                            ++upper;
                            ++lower;
                        }

                    }

                    upperList = merge;
                }

                for (Integer filename : upperList)
                    results.add(filename.toString());

                break;
            default:
                break;
        }



        return results;
    }

    // index the words using ArrayList
    private void indexWords2(String[] words, String filename,
                             HashMap<String, ArrayList<Integer>> index) {
        Integer filenameNum = Integer.valueOf(filename);

        for (int idx = 0; idx < words.length; ++idx) {
            ArrayList<Integer> filenames;
            if (index.containsKey(words[idx]))
                filenames = index.get(words[idx]);
            else {
                filenames = new ArrayList<>();
                filenames.add(filenameNum);
            }

            if (!filenames.contains(filenameNum)) {
                int f = 0;
                for (; f < filenames.size(); ++f) {
                    if (filenameNum < filenames.get(f)) {
                        filenames.add(f, filenameNum);
                        break;
                    }
                }

                if (f == filenames.size())
                    filenames.add(filenameNum);
            }

            index.put(words[idx], filenames);
        }
    }

    // retrieval with ArrayList + skip
    private HashSet<String> retrieve3(Vector<String> query,
                                      DocSearch.SearchType op) throws Exception {
        HashSet<String> results = new HashSet<>();

        switch (op) {
            case OR:
                for (int idx = 0; idx < query.size(); ++idx) {
                    String key = query.get(idx).toLowerCase();
                    ArrayList<Integer> tmp = index2_.get(key);
                    for (Integer filename : tmp)
                        results.add(filename.toString());
                }
                break;
            case AND:
                ArrayList<Integer> upperList = index2_.get(query.get(0));
                for (int idx = 1; idx < query.size(); ++idx) {
                    // get the key
                    String key = query.get(idx).toLowerCase();

                    // get the list
                    ArrayList<Integer> lowerList = index2_.get(key);
                    ArrayList<Integer> merge = new ArrayList<>();

                    int upper = 0;  // cursor for upperList
                    int lower = 0;  // cursor for lowerList
                    int upperVal = 0;   // value from upperList
                    int lowerVal = 0;   // value from lowerList

                    int upperSize = upperList.size();
                    int lowerSize = lowerList.size();

                    // get the step
                    int upperStep = (int)Math.sqrt(upperSize);
                    int lowerStep = (int)Math.sqrt(lowerSize);

                    // get the skip
                    int upperSkip = upper;
                    int lowerSkip = lower;

                    // merge
                    while ((upper < upperSize) && (lower < lowerSize)) {
                        ++loopCounter_;

                        upperVal = upperList.get(upper);
                        lowerVal = lowerList.get(lower);
                        if (upperVal > lowerVal) {
                            if ((lower >= lowerSkip) && (lower != lowerSize - 1)) {   // at the skip point
                                // move the skip point
                                lowerSkip = lowerSkip + lowerStep < lowerSize ?
                                        lowerSkip + lowerStep : lowerSize - 1;
                                // if the inequality direction does not change
                                if (upperVal > lowerList.get(lowerSkip)) {
//                                    System.out.println("skipped!");
                                    lower = lowerSkip;
                                    continue;
                                }
                            }
                            ++lower;
                        }
                        else if (upperVal < lowerVal) {
                            if ((upper >= upperSkip) && (upper != upperSize - 1)) {   // at the skip point
                                // move the skip point
                                upperSkip = upperSkip + upperStep < upperSize ?
                                        upperSkip + upperStep : upperSize - 1;
                                // if the inequality direction does not change
                                if (upperList.get(upperSkip) < lowerVal) {
//                                    System.out.println("skipped!");
                                    upper = upperSkip;
                                    continue;
                                }
                            }
                            ++upper;
                        }
                        else {
                            merge.add(upperVal);
                            ++upper;
                            ++lower;
                        }
                    }

                    upperList = merge;
                }

                for (Integer filename : upperList)
                    results.add(filename.toString());

                break;
            default:
                break;
        }

        return results;
    }

    // retrieval with array + skip
    private HashSet<String> retrieve4(Vector<String> query,
                                      DocSearch.SearchType op) throws Exception {
        HashSet<String> results = new HashSet<>();

        switch (op) {
            case OR:
                for (int idx = 0; idx < query.size(); ++idx) {
                    String key = query.get(idx).toLowerCase();
                    int[] tmp = index3_.get(key);
                    for (int filename : tmp)
                        results.add(String.valueOf(filename));
                }
                break;
            case AND:
                int[] upperList = index3_.get(query.get(0));
                for (int idx = 1; idx < query.size(); ++idx) {
                    // get the key
                    String key = query.get(idx).toLowerCase();

                    // get the list
                    int[] lowerList = index3_.get(key);
                    ArrayList<Integer> merge = new ArrayList<>();

                    int upper = 0;  // cursor for upperList
                    int lower = 0;  // cursor for lowerList
                    int upperVal = 0;   // value from upperList
                    int lowerVal = 0;   // value from lowerList

                    int upperSize = upperList.length;
                    int lowerSize = lowerList.length;

                    // get the step
                    int upperStep = (int)Math.sqrt(upperSize);
                    int lowerStep = (int)Math.sqrt(lowerSize);

                    // get the skip
                    int upperSkip = upper;
                    int lowerSkip = lower;

                    // merge
                    while ((upper < upperSize) && (lower < lowerSize)) {
                        ++loopCounter_;

                        upperVal = upperList[upper];
                        lowerVal = lowerList[lower];
                        if (upperVal > lowerVal) {
                            if ((lower >= lowerSkip) && (lower != lowerSize - 1)) {   // at the skip point
                                // move the skip point
                                lowerSkip = lowerSkip + lowerStep < lowerSize ?
                                        lowerSkip + lowerStep : lowerSize - 1;
                                // if the inequality direction does not change
                                if (upperVal > lowerList[lowerSkip]) {
//                                    System.out.println("skipped!");
                                    lower = lowerSkip;
                                    continue;
                                }
                            }
                            ++lower;
                        }
                        else if (upperVal < lowerVal) {
                            if ((upper >= upperSkip) && (upper != upperSize - 1)) {   // at the skip point
                                // move the skip point
                                upperSkip = upperSkip + upperStep < upperSize ?
                                        upperSkip + upperStep : upperSize - 1;
                                // if the inequality direction does not change
                                if (upperList[upperSkip] < lowerVal) {
//                                    System.out.println("skipped!");
                                    upper = upperSkip;
                                    continue;
                                }
                            }
                            ++upper;
                        }
                        else {
                            merge.add(upperVal);
                            ++upper;
                            ++lower;
                        }
                    }

                    // might need optimize this part
                    upperList = new int[merge.size()];
                    for (int elm = 0; elm < merge.size(); ++elm)
                        upperList[elm] = merge.get(elm);
                }

                for (Integer filename : upperList)
                    results.add(filename.toString());

                break;
            default:
                break;
        }

        return results;
    }

}


// Experiment results
// 1. Time & Memory
//   1.1 with HashSet implementation from PA1
//     Time: 1971 msec, Memory: 12473040 B
//   1.2 with ArrayList
//     Time: 2285 msec, Memory:  4754928 B, Loops: 5314174
//   1.3 with ArrayList + skip
//     Time: 2134 msec, Memory:  4752496 B, Loops: 4022150
//   1.4 with array + primitive type + skip
//     Time: 2203 msec, Memory:  3202288 B, Loops: 4022150
// 2. Score
//   2.1 [^a-z]
//     11984
