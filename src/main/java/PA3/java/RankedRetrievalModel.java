package PA3.java;

/**Copyright 2014 smanna@csupomona.edu
 * You should implement your code here. 
 * Feel free to add your own methods etc.
 *
 * !!!WARNING!!! Please make sure you comment your code. 
 * If I cannot follow what you have done, I deduct 2 points.
 */

import java.io.File;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.tartarus.snowball.ext.englishStemmer;

public class RankedRetrievalModel implements DocSearch {
    // /////////////////////  PostingList CLASS  ////////////////////
    // Implementation of PostingList, that can hold a list of <docid:data>
    // pairs in form of a list. 'data' can be of any type. This class assumes
    // all docs are inserted in ascending order and hence makes a rudimentary
    // docid comparison with the last element.
    private class PostingList<T> {
        // ////////////  INTERNAL NODE CLASS  ////////////////////
        // Internal representation of every node in the list.
        private class Node {
            // data carried by this node.
            // could be of any type you need.
            Long docId_;
            T data_;

            // reference to the next node in the chain,
            // or null if there isn't one.
            Node next_;

            // Node constructor
            public Node(Long docid, T data) {
                next_ = null;
                docId_ = docid;
                data_ = data;
            }

            // these methods should be self-explanatory
            public Long getDocId() {
                return docId_;
            }

            public T getData() {
                return data_;
            }

            public Node getNext() {
                return next_;
            }

            public void setNext(Node next) {
                next_ = next;
            }
        }
        // ////////////  END of INTERNAL NODE CLASS  ////////////////////

        // reference to the head node.
        private Node head_;
        private Node tail_;
        private int listSize_;
        //public enum SearchType {OR, AND}

        // LinkedList constructor
        public PostingList() {
            // this is an empty list, so the reference to the head node
            // is set to a new node with no data
            head_ = null;
            tail_ = null;
            listSize_ = 0; // size of the list
        }

        // Copy constructor
        public PostingList(PostingList<T> sl) {
            head_ = null;
            tail_ = null;
            Node current = sl.head_;
            while (current != null) {
                appendIfNew(current.getDocId(), current.getData());
                current = current.getNext();
            }
        }

        public String toString() {
            String ret = "[ ";
            Node p = head_;
            for (; p != null; p = p.getNext()) {
                ret = ret + "[" + p.getDocId() + ": " + p.getData() + " -> ";
            }
            return ret + "null ]";
        }

        // ASSUMPTION: docid will be passed in incremental order.
        private void appendIfNew(Long docId, T data) {
            // post: appends the specified element to the end of this list.
            Node temp = new Node(docId, data);
            if (head_ == null) {
                // First entry
                head_ = temp;
                tail_ = head_;
                listSize_ = 1;
                return;
            }
            if (tail_.getDocId() < docId) {
                tail_.setNext(temp);
                tail_ = temp;
                listSize_++;
            }
        }

        public HashMap<Long, T> getDocSet() {
            HashMap<Long, T> retDocs = new HashMap<Long, T>();
            Node current = head_;
            while (current != null) {
                retDocs.put(current.getDocId(), current.getData());
                current = current.getNext();
            }
            return retDocs;
        }

        public boolean containsDoc(Long docId) {
            Node current = head_;
            while (current != null) {
                if (current.getDocId() == docId)
                    return true;
            }

            return false;
        }

        public Node getDocNode(Long docId) {
            Node current = head_;
            while (current != null) {
                if (current.getDocId() == docId)
                    return current;
            }

            return null;
        }

        public void append(Node doc) {
            if (tail_ != null) {
                tail_.setNext(doc);
                tail_ = doc;
                ++listSize_;
            } else {
                head_ = doc;
                tail_ = doc;
                listSize_ = 1;
            }
        }
    }
    ///////////// END of PostingList IMPLEMENTATION //////////////

    // TODO(student): Internal data structure declaration
    private String stopWordFile_;
    private HashMap<String, float[]> tfIdf_;
    private int[] idx2Doc_;  // docId, docIdx
    private int n_; // num of documents
    private int[] docLens_;
    private HashSet<String> stopWordSet;

    public RankedRetrievalModel() {
        stopWordFile_ = "src/main/java/PA3/java/stopWords.txt"; // stop word list
        // TODO(student): Construct all your internal data-structure for indexing.
        tfIdf_ = new HashMap<>();
        n_ = 0;
    }

    public void index(String dir) throws Exception {
        HashMap<String, int[]> tf = new HashMap<>();    // first construct a tf matrix

        stopWordSet = readFileAsSetOfWords(stopWordFile_);

        File inDir = new File(dir);
        File[] infiles = inDir.listFiles();
        int docIdx = 0;
        n_ = infiles.length;
        idx2Doc_ = new int[n_];
        docLens_ = new int[n_];
        for (File infile : infiles) {
            if (infile.getName().charAt(0) == '.') {
                continue;
            }
            Vector<String> tokens = tokenizeADoc(infile.getAbsolutePath());

            // TODO(student): Process docid, infiles[docid] & tokens for indexing.
            int docId = Integer.valueOf(infile.getName());

            for (String token : tokens) {
                int[] docArray;
                if (tf.containsKey(token)) {
                    docArray = tf.get(token);
                    ++docArray[docIdx];
                } else {
                    docArray = new int[n_];
                    ++docArray[docIdx];
                }
                tf.put(token, docArray);
            }

            // record docIdx for fast retrieval
            idx2Doc_[docIdx] = docId;
            ++docIdx;
        }

        // convert to tf-idf matrix
        for (String token : tf.keySet()) {
            // count df
            int df = 0;
            int[] docArray = tf.get(token);
            for (int idx = 0; idx < n_; ++idx) {
                if (docArray[idx] > 0) {
                    ++df;
                    ++docLens_[idx];
                }

            }

            // compute idf
            double idf = Math.log((double) n_ / df);

            // compute tf-idf
            float[] tfidfArray = tfIdf_.containsKey(token) ? tfIdf_.get(token) : new float[n_];
            for (docIdx = 0; docIdx < n_; ++docIdx) {
                // compute logarithmic scaled tf
                double ltf = docArray[docIdx] > 0 ? 1 + Math.log(docArray[docIdx]) : 0;
                tfidfArray[docIdx] = (float) (ltf * idf);
            }
            tfIdf_.put(token, tfidfArray);

        }
    }

    public Vector<String> retrieve(Vector<String> query) throws Exception {
        HashMap<String, Double> docScore = scoreAllDocuments(query);
        return sortByValue(docScore);
    }

    private HashMap<String, Double> scoreAllDocuments(Vector<String> query) {
        HashMap<String, Double> docToScore = new HashMap<String, Double>();
        // TODO(student): Populate docToScore with score keyed by filename.

        // collect query terms
        HashSet<String> queryTerms = new HashSet<>();
        for (String term : query)
            if (!stopWordSet.contains(term))
                queryTerms.add(stemWord(term));


        // cosine similarity using Figure 7.1 algorithm
        for (int docIdx = 0; docIdx < n_; ++docIdx) {

            double dotProduct = 0.0;
            double docLen = docLens_[docIdx];
            for (String term : queryTerms)
                if (tfIdf_.containsKey(term))
                    dotProduct += tfIdf_.get(term)[docIdx]; // sum

            if (docLen != 0) {
                double cos = dotProduct / docLen;
                docToScore.put(String.valueOf(idx2Doc_[docIdx]), cos);
            }
        }
        return docToScore;
    }

    // sorting by value
    private Vector<String> sortByValue(final HashMap<String, Double> docScore) {
        TreeMap<Double, Vector<String>> sortedByScore =
                new TreeMap<Double, Vector<String>>();
        for (Map.Entry<String, Double> entry : docScore.entrySet()) {
            if (!sortedByScore.containsKey(entry.getValue())) {
                sortedByScore.put(entry.getValue(), new Vector<String>());
            }
            sortedByScore.get(entry.getValue()).add(entry.getKey());
        }
        Vector<String> rankedDocs = new Vector<String>();
        ArrayList<Double> keys = new ArrayList<Double>(sortedByScore.keySet());
        int count = 0;
        for (int i = keys.size() - 1; i >= 0; --i) {
            for (String doc : sortedByScore.get(keys.get(i))) {
                rankedDocs.add(doc);
                ++count;
                if (count >= 20) {
                    break;
                }
            }
        }
        return rankedDocs;
    }

    private englishStemmer es = new englishStemmer();

    // stem words
    private String stemWord(String word) {
        String stemmedWord = "";
        es = new englishStemmer();
        es.setCurrent(word);
        es.stem();
        stemmedWord = es.getCurrent();
        return stemmedWord;
    }

    // Given an input file, reading the contents of the file and storing into an
    // HashSet
    private HashSet<String> readFileAsSetOfWords(String infile) {
        try {
            HashSet<String> contents = new HashSet<String>();
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(new FileInputStream(infile)));
            String str;
            while ((str = br.readLine()) != null) {
                contents.add(str.toLowerCase());
            }
            br.close();
            return contents;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Returns stemmed tokens and stop-words removed
    private Vector<String> tokenizeADoc(String infile) throws Exception {

        Vector<String> tokens = new Vector<String>();
        // read the file
        File file = new File(infile);
        Scanner sc = new Scanner(file);
        while (sc.hasNext()) {
            String str = sc.nextLine();
            // tokenize the string
            StreamTokenizer tokenizer =
                    new StreamTokenizer(new StringReader(str));
            tokenizer.lowerCaseMode(true);
            tokenizer.whitespaceChars(0, 64);
            tokenizer.wordChars(65, 90);
            tokenizer.whitespaceChars(91, 96);
            tokenizer.wordChars(97, 122);
            tokenizer.whitespaceChars(123, 255);
            int tt = tokenizer.nextToken();
            while (tt != StreamTokenizer.TT_EOF) {
                // ignoring the stop words before generating word freq map
                if (tt == StreamTokenizer.TT_WORD) { // checking if tt is word
                    // if word is not in stop word list & not already added in
                    // wordSet, add it
                    String word = tokenizer.sval;
                    String stemmedWord = stemWord(tokenizer.sval);
                    if (!stopWordSet.contains(word)) {
                        tokens.add(stemmedWord);
                    }
                    tt = tokenizer.nextToken();
                }
            }
        }
        sc.close();
        return tokens;
    }

}
