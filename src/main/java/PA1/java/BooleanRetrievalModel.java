package PA1.java;

import java.io.*;
import java.util.*;

/**
 * The DocStat class holds all the document statistics
 * @author smanna
 *
 */

public class BooleanRetrievalModel {
    public enum SearchType {OR, AND}

    private final HashMap<String, HashSet<String>> index_;
//    private final HashMap<Stirng, Linked>

    // Constructor();
    public BooleanRetrievalModel() {
        index_ = new HashMap<>();
    }

    // index the words using HashSet
    private void index(String[] words, String filename) {
        for (int idx = 0; idx < words.length; ++idx) {
            HashSet<String> filenames;
            if (index_.containsKey(words[idx]))
                filenames = index_.get(words[idx]);
            else
                filenames = new HashSet<>();
            filenames.add(filename);
            index_.put(words[idx], filenames);
        }
    }

    /*
    * @dir: Directory containing many files to index.
    * This method reads all the files under 'dir' and index them for efficient
    * search in future.
    */
    public void buildIndex(String dir) throws Exception {
        File[] files = new File(dir).listFiles();

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
                    index(words, filename);
                }
            }
        }
    }

    /*
    * Returns HashSet of String containing filenames.
    * Note that these filenamses within the 'dir' passed in buildIndex(). These
    * filenames are not full path and should not contain 'dir' within it.
    */
    public HashSet<String> Search(Vector<String> query, SearchType op)
      throws Exception {

        String firstKey = query.get(0).toLowerCase();
        HashSet<String> results = new HashSet<>(index_.get(firstKey));

        for (int idx = 1; idx < query.size(); ++idx) {
            String key = query.get(idx).toLowerCase();
            switch (op) {
                case OR:
                    results.addAll(index_.get(key));
                    break;
                case AND:
                    HashSet<String> temp = new HashSet<>();
                    for (String filename : results)
                        if (index_.get(key).contains(filename))
                            temp.add(filename);
                    results = temp;
                    break;
                default:
                    break;
            }
        }

        return results;
    }
}


