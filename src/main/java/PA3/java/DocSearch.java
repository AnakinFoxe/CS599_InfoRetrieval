package PA3.java;

import java.util.Vector;

public interface DocSearch {
    public static enum SearchType {OR, AND}

    public void index(String dir) throws Exception;

    public Vector<String> retrieve(Vector<String> query) throws Exception;
}
