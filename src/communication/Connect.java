package communication;

public class Connect extends ServerConnection {
	
	public Connect()
	{
		cmd = "CONNECT 2112\r\n\r\n";
	}
	
	protected void handleResponse(String response)
	{
		
	}
}
