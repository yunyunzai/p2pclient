package settings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class Settings {
    
    public static String SERVER_IP;
    public static int SERVER_PORT;
    public static int CLIENT_PEER_PORT;
    public static int MAX_PEER_CONNECTION;
    public static int SERVER_LIST_TIMEOUT;
    
    public static String SHARED_FOLDER;
    
    public static final String CONF_FILE = "client.conf";
    
    /**
     * Reads the settings file to populate the static fields of the Settings class
     * @throws FileNotFoundException if the rc file cannot be located
     * @throws InvalidRCException if there is an error parsing the rc file
     */
    public static void loadSettings() throws FileNotFoundException, InvalidRCException {
        Yaml yaml = new Yaml();
        BufferedReader r = new BufferedReader(new FileReader(CONF_FILE));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) yaml.load(r);
        try {
            SERVER_IP = (String) map.get("server_ip");
            SERVER_PORT = (Integer) map.get("server_port");
            CLIENT_PEER_PORT = (Integer) map.get("client_port");
            MAX_PEER_CONNECTION = (Integer) map.get("max_peers");
            SERVER_LIST_TIMEOUT = (Integer) map.get("server_timeout");
            SHARED_FOLDER = (String) map.get("shared_folder");
        } catch (ClassCastException e) {
            throw new InvalidRCException();
        }
    }
}
