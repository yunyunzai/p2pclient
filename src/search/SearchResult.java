package search;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;

public class SearchResult implements JSONAware, JSONStreamAware {

    private File file;
    private String hash;
    
    public SearchResult(File file, String hash) {
        this.file = file;
        this.hash = hash;
    }
    
    public String getName() {
        return file.getName();
    }
    
    public String getSize() {
        return "" + file.length();
    }
    
    public String getHash() {
        return hash;
    }
    
    @Override
    public void writeJSONString(Writer out) throws IOException {
        LinkedHashMap<String, Object> obj = new LinkedHashMap<String, Object>();
        obj.put("name", file.getName());
        obj.put("size", file.length());
        obj.put("hash", hash);
        JSONValue.writeJSONString(obj, out);
    }

    @Override
    public String toJSONString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("{");
        
        sb.append(JSONObject.escape("name"));
        sb.append(":");
        sb.append("\"" + JSONObject.escape(file.getName()) + "\"");
        
        sb.append(",");
        
        sb.append(JSONObject.escape("size"));
        sb.append(":");
        sb.append(file.length());
        
        sb.append(",");
        
        sb.append(JSONObject.escape("hash"));
        sb.append(":");
        sb.append("\"" + JSONObject.escape(hash) + "\"");
        
        sb.append("}");
        
        return sb.toString();
    }

}
