package search;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import settings.Settings;

public class LocalShares {

    private static final String delimiters = "[ ._]";
    private static final int MIN_KEY_LEN = 3;
    
    private static Map<String, Set<File>> index = new HashMap<String, Set<File>>();
    
    public static void buildIndex() {
        indexDirectory(new File(Settings.SHARED_FOLDER));
    }
    
    private static void indexDirectory(File dir) {
        for (File file : dir.listFiles()) {
            
            // XXX: might follow circular symlink
            if (file.isDirectory()) {
                indexDirectory(file);
                continue;
            }
            
            // add file to file set for each key in file name
            for (String key : file.getName().split(delimiters)) {
                if (key.length() < MIN_KEY_LEN)
                    continue;
                
                Set<File> values = index.get(key);
                if (values == null) {
                    values = new HashSet<File>();
                    index.put(key, values);
                }
                System.out.println("Associating "+file.getName()+" with key "+key);
                values.add(file);
            }
        }
    }
    
    public static Set<File> query(String query) {
        String[] keywords = query.split(delimiters);
        Set<File> results = new HashSet<File>();
        
        for (String key : keywords) {
            if (key.length() >= MIN_KEY_LEN && index.containsKey(key))
                results.addAll(index.get(key));
        }
        return results;
    }
}
