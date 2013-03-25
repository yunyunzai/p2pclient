package communication;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import settings.Settings;

import data.ClientInfo;

public class List extends ServerConnection {
	
	private ArrayList<ClientInfo> clientList;
	
	
	public List()
	{
		cmd = "LIST " + Settings.CLIENT_PEER_PORT +"\r\n\r\n";
		clientList = new ArrayList<ClientInfo>();
		
		System.out.println("CREATING LIST");
	}

	@Override
	protected void handleResponse(String response)
	{
		response = response.trim();
		
		try
		{
			JSONParser parser = new JSONParser();
			JSONArray jsonClientList = (JSONArray)parser.parse(response);

			System.out.println("jsonClientList:" + jsonClientList.toString());
			Iterator itr = jsonClientList.iterator();
			
			while (itr.hasNext())
			{
				JSONObject addr = (JSONObject) itr.next();
				
				ClientInfo c_info = new ClientInfo();
				c_info.setIp((String)addr.get("ip"));
				c_info.setPort(((Number)addr.get("port")).intValue());
				clientList.add(c_info);
				
				System.out.println(c_info.getIp() + ":" + c_info.getPort());
			}
		}
		catch (ParseException e)
		{
			System.out.println("ERROR");
			System.out.println("position: " + e.getPosition());
			System.out.println(e.getMessage());
		}
	}
	
	public ArrayList<ClientInfo> getClientList()
	{
		return clientList;
	}
}
