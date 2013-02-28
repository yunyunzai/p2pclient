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

import data.ClientInfo;


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
		Connect connectCmd = new Connect();
		try
		{
			connectCmd.send();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			throw new Exception("Could not connect to the server: " + e.getMessage()); 
		}
		
		//Create listening connection
//    	createListeningConnection(2112);
	}

	public ArrayList<ClientInfo> listPeers() throws Exception
	{
		List listCmd = new List();
		try
		{
			listCmd.send();
			
			return listCmd.getClientList();
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			throw new Exception("Could not connect to the server: " + e.getMessage()); 
		}
	}


	public void disconnectServer() throws Exception
	{

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
	
}
