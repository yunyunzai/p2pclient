package search;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import settings.Settings;

public class LocalShares {
    
    private static final String delimiters = "[ ._]";
    private static final int MIN_KEY_LEN = 2;
    
    /**
     * Index used for keyword search.  Keywords are associated with sets of strings,
     * where each string in the set is the full (absolute) path name of a file that
     * matches the keyword.  Keywords are automatically converted to lower case.
     */
    private static Map<String, Set<String>> searchIndex =
            new HashMap<String, Set<String>>() {
        private static final long serialVersionUID = 1L;
        @Override
        public Set<String> put(String key, Set<String> value) {
            return super.put(key.toLowerCase(), value);
        }

        @Override
        public Set<String> get(Object key) {
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
            
            lookupIndex.put(file.getAbsolutePath(), file);
            
            // add file to file set for each key in file name
            for (String key : file.getName().split(delimiters)) {
                if (key.length() < MIN_KEY_LEN || ignoredKeys.contains(key))
                    continue;
                
                Set<String> values = searchIndex.get(key);
                if (values == null) {
                    values = new HashSet<String>();
                    searchIndex.put(key, values);
                }
                System.out.println("Associating "+file.getName()+" with key "+key);
                values.add(file.getAbsolutePath());
            }
        }
    }
    
    /**
     * Looks up a file in the index given an absolute path
     * @param path the absolute path of the File to be retrieved
     * @return the File object corresponding to the given path, or null if
     *         no such file is indexed
     */
    public static File getFile(String path) {
        return lookupIndex.get(path);
    }
    
    /**
     * Queries the index for files matching the given keywords
     * @param query a space-separated list of keywords
     * @return a set of files that matched the query
     */
    public static Set<String> query(String query) {
        String[] keywords = query.split(delimiters);
        Set<String> results = new HashSet<String>();
        
        for (String key : keywords) {
            if (searchIndex.containsKey(key))
                results.addAll(searchIndex.get(key));
        }
        return results;
    }
}
