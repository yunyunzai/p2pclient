package peerclient.commands;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import data.ClientInfo;

import search.SearchResult;
import settings.Settings;
import ui.ClientUI;

public class SearchCmd implements Runnable {
	protected String cmd;
	protected Socket sock;
	private String searchString;
	private String ip;
	private int port;
	
	public SearchCmd(String ip, int port, String searchString)
	{
		this.ip = ip;
		this.port = port;
		this.searchString = searchString;
		cmd = "SEARCH " + searchString + "\r\n\r\n";
	}
	
	public void run()
	{
		sock = null;
		InputStream in = null;
		OutputStream out = null;
		String responseString = null;
		
		try
		{
			sock = new Socket(this.ip, this.port);
			
			out = sock.getOutputStream();  
            out.write(cmd.getBytes("ASCII"));            
            out.flush();
            
            in = sock.getInputStream();
            
            byte[] response = new byte[100];
            int bytesRead = 0;
            while(bytesRead < 3)
            {
            	if(bytesRead == -1)
                {
                	throw new Exception("Connection closed by the other side.");
                }
            	
            	bytesRead += in.read(response, bytesRead, 100);
            }
            
            responseString = new String(response, "ASCII").trim();

            if(responseString.equals("ER"))
            {
            	throw new Exception("An error occurred on the other side.");
            }
            else if(responseString.equals("OK"))
            {
            	throw new Exception("The other side didn't respond properly.");
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
		finally
		{              
            try
            {
            	sock.close();
            	in.close();
            	out.close();
            	
            	handleResponse(responseString);
            }
            catch(Exception e)
            {	
            	System.out.println(e.getMessage());
            }
        }
	}
	
	public void handleResponse(String responseString)
	{
		 System.out.println("SearchCmd response: " + responseString);
		 
		 try
		{
			JSONParser parser = new JSONParser();
			JSONArray searchResultList = (JSONArray)parser.parse(responseString);
//			JSONArray searchResultList = (JSONArray)parser.parse("[{\"name\":\"laptop.txt\",\"size\":0,\"hash\":\"da39a3ee5e6b4b0d3255bfef95601890afd80709\"}]");

			System.out.println("searchResult:" + searchResultList.toString());
			Iterator itr = searchResultList.iterator();
			
			while (itr.hasNext())
			{
				JSONObject result = (JSONObject) itr.next();
				
				System.out.println("Name:" + result.get("name"));
				System.out.println("Size:" + result.get("size"));
				System.out.println("Hash:" + result.get("hash"));
			}
		}
		catch (ParseException e)
		{
			System.out.println("ERROR");
			System.out.println("position: " + e.getPosition());
			System.out.println(e.getMessage());
		}
	}
	
	public Socket getSocket()
	{
		return sock;
	}
}
