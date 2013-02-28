package communication;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import data.ClientInfo;

public class List extends ServerConnection {
	
	private ArrayList<ClientInfo> clientList;
	
	
	public List()
	{
		cmd = "LIST\r\n\r\n";
		clientList = new ArrayList<ClientInfo>();
		
		System.out.println("CREATING LIST");
	}
	
	protected void handleResponse(String response)
	{
		response = response.trim();
		
		try
		{
			JSONParser parser = new JSONParser();
			JSONArray jsonClientList = (JSONArray)parser.parse(response);
//			JSONArray jsonClientList = (JSONArray)parser.parse("[{\"ip\":4112996120,\"port\":2112},{\"ip\":654321,\"port\":2000}]");
			System.out.println("jsonClientList:" + jsonClientList.toString());
			Iterator itr = jsonClientList.iterator();
			
			while (itr.hasNext())
			{
				JSONObject addr = (JSONObject) itr.next();
				System.out.println(addr.get("ip") + ":" + addr.get("port"));
				
				ClientInfo c_info = new ClientInfo();
				c_info.setIp((String)addr.get("ip"));
				c_info.setPort(Integer.parseInt((String)addr.get("port")));
				
				System.out.println(c_info.getIp() + ":" + c_info.getPort());
				
				clientList.add(c_info);
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
