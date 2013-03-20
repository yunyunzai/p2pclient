package search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import org.json.simple.JSONArray;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import settings.InvalidRCException;
import settings.Settings;

public class LocalShares {
    
    private static final String delimiters = "[ ._]";
    private static final int MIN_KEY_LEN = 2;
    
    /**
     * Index used for keyword search.  Keywords are associated with sets of strings,
     * where each string in the set is the full (absolute) path name of a file that
     * matches the keyword.  Keywords are automatically converted to lower case.
     */
    private static Map<String, Set<SearchResult>> searchIndex =
            new HashMap<String, Set<SearchResult>>() {
        private static final long serialVersionUID = 1L;
        @Override
        public Set<SearchResult> put(String key, Set<SearchResult> value) {
            return super.put(key.toLowerCase(), value);
        }

        @Override
        public Set<SearchResult> get(Object key) {
            return super.get(((String)key).toLowerCase());
        }
    };
    
    /** File index used for lookup; keys are full (absolute) path names */
    private static Map<String, File> lookupIndex = new HashMap<String, File>();
    
    /** Set of keywords which should not be indexed (in the search index) */
    private static final Set<String> ignoredKeys = new HashSet<String>
            (Arrays.asList("a", "the", "mp3")) {
        private static final long serialVersionUID = 1L;
        @Override
        public boolean contains(Object key) {
            return super.contains(((String)key).toLowerCase());
        }
    };
    
    /**
     * Builds a file index, so that file search and lookup are efficient
     */
    public static void buildIndex() {
        File dir = new File(Settings.SHARED_FOLDER);
        if (!dir.exists()) {
            System.out.println("Shared directory '"+Settings.SHARED_FOLDER+"' not found");
            return;
        }
        indexDirectory(new File(Settings.SHARED_FOLDER));
    }
    
    /**
     * Indexes a given directory and, recursively, any subdirectories
     */
    private static void indexDirectory(File dir) {
        for (File file : dir.listFiles()) {
            
            // XXX: might follow circular symlink?
            if (file.isDirectory()) {
                indexDirectory(file);
                continue;
            }
            
            String hash;
            try { hash = Files.hash(file, Hashing.sha1()).toString(); }
            catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            
            lookupIndex.put(hash, file);
            System.out.println("Hash of '"+file.getName()+"' is "+hash);
            
            // add file to file set for each key in file name
            for (String key : file.getName().split(delimiters)) {
                if (key.length() < MIN_KEY_LEN || ignoredKeys.contains(key))
                    continue;
                
                Set<SearchResult> values = searchIndex.get(key);
                if (values == null) {
                    values = new HashSet<SearchResult>();
                    searchIndex.put(key, values);
                }
                System.out.println("Associating "+file.getName()+" with key "+key);
                values.add(new SearchResult(file, hash));
            }
        }
    }
    
    /**
     * Looks up a file in the index given an absolute path
     * @param path the absolute path of the File to be retrieved
     * @return the File object corresponding to the given path, or null if
     *         no such file is indexed
     */
    public static File getFile(String hash) {
        return lookupIndex.get(hash);
    }
    
    /**
     * Queries the index for files matching the given keywords
     * @param query a space-separated list of keywords
     * @return a set of files that matched the query
     */
    /*public static Set<File> query(String query) {
        String[] keywords = query.split(delimiters);
        Set<File> results = new HashSet<File>();
        
        for (String key : keywords) {
            if (searchIndex.containsKey(key))
                results.addAll(searchIndex.get(key));
        }
        return results;
    }*/
    
    @SuppressWarnings("unchecked")
    public static JSONArray query(String query) {
        String[] keywords = query.split(delimiters);
        JSONArray results = new JSONArray();
        
        for (String key : keywords) {
            if (!searchIndex.containsKey(key))
                continue;
            results.addAll(searchIndex.get(key));
        }
        return results;
    }
    
    public static void main(String[] args) throws FileNotFoundException, InvalidRCException {
        settings.Settings.loadSettings();
        buildIndex();
        JSONArray res = query("test dert");
        System.out.println(res);
    }
}
