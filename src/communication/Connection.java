package communication;

import java.util.ArrayList;

import settings.Settings;

import data.ClientInfo;

public class Connection 
{
	KeepAliveThread keepAlive;

	public Connection()
	{
		keepAlive = null;
	}

	public void connectServer() throws Exception
	{
		Connect connectCmd = new Connect();
		try
		{
			connectCmd.send();
			keepAlive();
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
		dontKeepAlive();
	}
	
	private void keepAlive()
	{	
		keepAlive = new KeepAliveThread();
		keepAlive.start();
	}
	
	private void dontKeepAlive()
	{
		if (keepAlive != null && keepAlive.isAlive())
		{
			keepAlive.exit();		
		}
	}
	
	private class KeepAliveThread extends Thread
	{

		boolean exit = false;
		
		@Override
		public void run() 
		{
			Connect connectCmd = new Connect();
			while (!exit)
			{
				try 
				{
					//Waits for the timeout (or near it)
					KeepAliveThread.sleep(Settings.SERVER_LIST_TIMEOUT * 1000);
					
					//And tries to send another CONNECT
					connectCmd.send();
				} 
				catch (Exception e) 
				{
					System.out.println("Error while sending update command to the server: " + e.getMessage());
					break;
				}
			}			
		}
		
		public void exit()
		{
			exit = true;
		}
	}	
}
