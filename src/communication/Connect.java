package communication;

import settings.Settings;

public class Connect extends ServerConnection {
	
	public Connect()
	{
		cmd = "CONNECT "+Settings.CLIENT_PEER_PORT+"\r\n\r\n";
	}
	
	protected void handleResponse(String response)
	{
		
	}
}
