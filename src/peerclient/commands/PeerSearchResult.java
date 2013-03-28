package peerclient.commands;


public class PeerSearchResult {
	private String ip;
	private int port;
	private String filename;
	private int size;
	private String hash;
	
	public PeerSearchResult(String ip, int port, String filename, int size, String hash) {
		this.ip = ip;
		this.port = port;
        this.filename = filename;
        this.size = size;
        this.hash = hash;
    }
	
	public String getIP() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}
    
    public String getName() {
        return filename;
    }
    
    public String getSize() {
        return "" + size;
    }
    
    public String getHash() {
        return hash;
    }
}
