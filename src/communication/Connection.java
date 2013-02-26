package communication;

import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ui.ClientUI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


import java.awt.*;

public class Connection 
{	
	//Used to create the listening connection
	private ServerSocket peerServer;
	private Socket peerSocket;
	private String DEFAULT_SERVER_IP="54.245.150.162";
	private int DEFAULT_SERVER_PORT=2013;

	public Connection()
	{		  
		peerServer = null;  
		peerSocket = null;
	}

	public void connectServer() throws Exception
	{
		Socket s = initializeConnection();		
		sendCommand(s,"CONNECT 2100\r\n\r\n");
		handleConnectResponse(s);
		s.close();

	}

	public ArrayList<ClientInfo> listPeers() throws Exception
	{
		ArrayList<ClientInfo> result=new ArrayList<ClientInfo>();
		Socket s = initializeConnection();
		initializeConnection();
		sendCommand(s,"LIST\r\n\r\n");
		result=handleListResponse(s);
		s.close();
		return result;
	}


	public void disconnectServer() throws Exception
	{

	}

	/////////////////////////////////////////////////////////
	// helpers
	private Socket initializeConnection() throws Exception
	{
		Socket s=null;
		try
		{
			s = new Socket(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT);
		}
		catch (Exception e)
		{
			throw new Exception("Error initializing socket: "+e.getMessage());
		}
		finally
		{			
			System.out.println("Socket initialized");
		}
		return s;
	}


	private void sendCommand(Socket s,String cmd) throws Exception
	{		
		OutputStream out = null;

		try
		{            
			//Create the socket with the server
			//TODO: Maybe put the IP and port in a separate file and read from it so we have a configurable client
			//			s = new Socket("128.189.161.17", 2111);  

			//Get the data output stream  
			out = s.getOutputStream();  

			//Send a command to the server
			//TODO: Maybe put all the commands in a separate class so we can have more control of them
			out.write(cmd.getBytes("ASCII"));            
			out.flush(); 
		}
		catch(Exception e)
		{    
			throw new Exception("Error sending command: "+cmd);
		}

	}

	private void handleConnectResponse(Socket s) throws Exception
	{

		InputStream in = null;
		String responseString="";
		try{
			//Get the response input stream
			in = s.getInputStream();
			//TODO: make size of response configurable as well
			byte[] response = new byte[10000];

			int bytesRead = in.read(response);
			//TODO: make the size of the response as a constant
			while(bytesRead < 3)
			{
				if(bytesRead == -1)
				{
					throw new Exception("Connection closed by the other side.");
				}
				bytesRead += in.read(response, bytesRead, 10000);
			}
			responseString = new String(response, "ASCII").substring(0,bytesRead-1);

			System.out.println(bytesRead);
			//TODO: check why the heck the response is not matching any of both
			if(responseString.equals("ER\r\n"))
			{
				throw new Exception("An error occurred on the other side.");
			}
			else if(!responseString.equals("OK\r\n"))
			{
				throw new Exception("The other side didn't respond properly.");
			}
		}
		catch(Exception e)
		{
			throw new Exception("Error handling the CONNECT response: "+e.getMessage());
		}
		finally 
		{
			System.out.println("Correct Response received: "+responseString);
		}

		//Create listening connection
		createListeningConnection(2113);
	}

	private void createListeningConnection(int port)
	{
//		ServerSocket ssock;
//		try {
//			ssock = new ServerSocket(port);
//
//			System.out.println("port " + port + " opened");
//			
//			Socket sock = ssock.accept();
//			System.out.println("Someone has made socket connection");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		//TODO: create a listening connection for peers after telling the server you're alive
	}
	
	
	private ArrayList<ClientInfo> handleListResponse(Socket s) throws Exception
	{
		ArrayList<ClientInfo> clientList=new ArrayList<ClientInfo>();
		InputStream in = null;
		String responseString="";
		try{
			//Get the response input stream
			in = s.getInputStream();
			//TODO: make size of response configurable as well
			byte[] response = new byte[100];

			int bytesRead = in.read(response);
			//TODO: make the size of the response as a constant
			while(bytesRead < 3)
			{
				if(bytesRead == -1)
				{
					throw new Exception("Connection closed by the other side.");
				}
				bytesRead += in.read(response);
			}
			responseString = new String(response).substring(0,bytesRead-1);
			JsonParser jParser=new JsonParser();
			JsonElement jElement=jParser.parse(responseString);
			JsonArray jArray=jElement.getAsJsonArray();
			
			// parse clients ip and port into an ArrayList of ClientInfo object
			for (int i=0;i<jArray.size();i++)
			{
				ClientInfo c_info=new ClientInfo();
				String clientInfoStr = jArray.get(i).toString();
				int ipStart=clientInfoStr.indexOf("\"ip\":\"")+6;
				int ipEnd=clientInfoStr.indexOf("\",");
				System.out.println(clientInfoStr.substring(ipStart, ipEnd));
				c_info.setIp(clientInfoStr.substring(ipStart, ipEnd));
				
				int portStart=clientInfoStr.indexOf("\"port\":")+7;
				int portEnd=clientInfoStr.indexOf("}");
				System.out.println(clientInfoStr.substring(portStart, portEnd));
				c_info.setPort(Integer.parseInt(clientInfoStr.substring(portStart, portEnd)));
			}
			
			return clientList;
		}
		catch(Exception e)
		{
			throw new Exception("Error handling the LIST response: "+e.getMessage());
		}
		finally 
		{
			System.out.println("Correct Response received: "+responseString);
		}

	}
	
	private class ClientInfo
	{
		private String ip;
		private int port;
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
	}
	

}
