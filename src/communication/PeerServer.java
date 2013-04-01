package communication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import download.UploadManager;

import search.LocalShares;
import settings.Settings;


public class PeerServer {	

	private ServerSocket peerServerSocket;
	private HashMap<InetAddress,ClientThread> peerThreads;
	private Thread peerServerThread;
	private boolean peerServerUp = false;
	private int numPeerConnection=0;
	public static ArrayList<UploadManager> listofUploads=new ArrayList<UploadManager>(); 


	public PeerServer()
	{
		try {
			peerServerSocket = new ServerSocket(Settings.CLIENT_PEER_PORT);
			System.out.println("Port: " + peerServerSocket.getLocalPort() + " opened for peer communication.");		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public void startPeerServer()
	{	
		peerThreads = new HashMap<InetAddress, ClientThread>();

		if (peerServerThread==null)
		{
			peerServerThread=new PeerServerThread();
			peerServerThread.start();
		}
		else if (!peerServerThread.isAlive())
		{
			peerServerThread.start();
		}
	}

	public void closePeerServer()
	{
		try {
			// close all the peer connections
			for (InetAddress peers : peerThreads.keySet())
			{
				peerThreads.get(peers).getSocket().close();
			}
			// close the peer server socket
			peerServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public class PeerServerThread extends Thread
	{

		public PeerServerThread()
		{
			super(new PeerServerTask());
		}

		public void start()
		{
			super.start();
		}		


	}

	private class PeerServerTask implements Runnable
	{
		@Override
		public void run() {
			// infinite loop to accept new incoming peer and allocate new peer threads for each peer
			while (true)
			{
				try {
					Socket peerSocket=peerServerSocket.accept();
					ClientThread oldPeerThread;

					// if the older connection exists, terminate the older connection
					if ((oldPeerThread=peerThreads.get(peerSocket.getInetAddress()))!=null)
					{						
						oldPeerThread.getSocket().close();
					}
					// start new peer connection thread
					ClientThread newClientThread=new ClientThread(peerSocket);
					peerThreads.put(peerSocket.getInetAddress(),newClientThread);
					newClientThread.start();
					System.out.println("Someone has made socket connection.\n IP: "+peerSocket.getInetAddress());

				} catch (IOException e) {
					System.out.println("Peer server has been terminated.");
					break;
				}
			}			
		}
	}



	private class ClientThread extends Thread
	{		
		private Socket clientSocket;
		public ClientThread(Socket clientSocket)
		{
			super(new ClientTask(clientSocket));	
			this.clientSocket=clientSocket;
		}

		public Socket getSocket()
		{
			return clientSocket;
		}
		public void start()
		{
			super.start();
		}


	}

	private class ClientTask implements Runnable
	{
		private Socket clientSocket;
		private InetAddress address;
		public ClientTask(Socket clientSocket)
		{
			this.clientSocket=clientSocket;
			try {
				this.clientSocket.setSoTimeout(3000);
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			address=clientSocket.getInetAddress();
		}

		@Override
		public void run() {
			// infinite loop to handle new incoming peer message
			handlePeerMessage();	
		}

		public void handlePeerMessage()
		{
			InputStream in = null;
			OutputStream out = null;
			String messageString = null;
			//Get the data output stream  
			try {
				out = clientSocket.getOutputStream();
				//Get the response input stream
				in = clientSocket.getInputStream();
				// infinite loop to wait for client message and handles them
				while(true)
				{
					//TODO: make size of response configurable as well
					byte[] messageBuffer = new byte[100];
					int bytesRead=0;
					//TODO: make the size of the response as a constant
					while (bytesRead<3)
					{
						bytesRead+=in.read(messageBuffer, bytesRead, 100);
					}	

					messageString = new String(messageBuffer, "ASCII").trim();

					System.out.println("Received message: "+messageString+" from client ip: "+clientSocket.getInetAddress());

					//TODO: add more cases for client to client communication
					if(messageString.equals("ER"))
					{
						throw new Exception("An error occurred on the other side.");
					}
					// search request
					else if(messageString.indexOf("SEARCH", 0)==0)
					{
						String query=messageString.substring(6);
						JSONArray jsarr=LocalShares.query(query);
						String outString = jsarr.toJSONString() + "\r\n\r\n";
						out.write(outString.getBytes("ASCII"));
						out.flush();
						System.out.println("Received Search request, reply: "+jsarr.toJSONString());
						this.clientSocket.close();
					}
					// handle download request
					else if(messageString.indexOf("DOWNLOAD", 0)==0)
					{
						String fileHash = messageString.substring(9);
						File file=LocalShares.getFile(fileHash);
						if (file ==null)
						{
							out.write("ER\r\n".getBytes("ASCII"));
							out.flush();
						}
						else
						{
							UploadManager um=new UploadManager(this.clientSocket,file);
							PeerServer.listofUploads.add(um);
							um.start_upload();
						}
					}
					else if(messageString.equals("OK"))
					{
						throw new Exception("The other side didn't respond properly.");
					}
				}
			} catch (IOException e) {
				peerThreads.remove(address);
				System.out.println("Client connection terminated.");				
			} 
			catch (Exception e) {

				e.printStackTrace();
			} 
		}
	}

}
