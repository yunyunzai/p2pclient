package peerclient;
import java.util.ArrayList;
import java.util.HashMap;

import data.ClientInfo;
import peerclient.commands.*;

public class PeerClient {
	private HashMap<String, Thread> peerSearchThreads;
	//private HashMap<String, Thread> peerDownloadThreads;
	
	
	public PeerClient()
	{
		peerSearchThreads = new HashMap<String, Thread>();
		//peerDownloadThreads = new HashMap<String, Thread>();
	}
	
	// search all clients
	public void searchAllPeers(ArrayList<ClientInfo> peerList, String searchString) throws Exception
	{
		for (ClientInfo peer : peerList) {
			Thread searchThread = new Thread(new SearchCmd(peer.getIp(), peer.getPort(), searchString));
			peerSearchThreads.put(peer.getIp() + Integer.toString(peer.getPort()), searchThread);
			searchThread.start();
			
			System.out.println("Starting Search thread on: " + peer.getIp() + ":" + peer.getPort());
		}
		
		CheckSearchThreads checkThreads = new CheckSearchThreads();
		checkThreads.start();
	}
	
	// start download with a client
	public void downloadFileFromPeer(String ip, int port, String fileHash, String fileName, int fileSizeBytes)
	{
		Thread downloadThread = new Thread(new DownloadCmd(ip, port, fileHash, fileName, fileSizeBytes));
		//peerDownloadThreads.put(ip + Integer.toString(port), downloadThread);
		downloadThread.start();
		
		System.out.println("starting Download thread on: " + ip + ":" + port);
	}
	
	public HashMap<String, Thread> getSearchThreads()
	{
		return peerSearchThreads;
	}
	
//	public HashMap<String, Thread> getDownloadThreads()
//	{
//		return peerDownloadThreads;
//	}
	
	private class CheckSearchThreads extends Thread
	{
		boolean exit = false;
		
		public void run() 
		{
			while (!exit)
			{
				try 
				{
					CheckSearchThreads.sleep(1000);
					
					boolean threadsComplete = true;
					for (Thread searchThread : peerSearchThreads.values()) {
						if (searchThread.isAlive()) {
							threadsComplete = false;
							break;
						}
					}
					
					// all searches have completed
					if (threadsComplete) {
						System.out.println("Search Completed");
						exit = true;
					}
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
