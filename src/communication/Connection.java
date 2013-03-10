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

	public Connection()
	{		  
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
	
	
}
