package peerclient.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import settings.Settings;

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
		cmd = "SEARCH\r\n\r\n";
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
            int bytesRead = in.read(response);
            while(bytesRead < 3)
            {
            	if(bytesRead == -1)
                {
                	throw new Exception("Connection closed by the other side.");
                }
            	
            	bytesRead += in.read(response, bytesRead, 100);
            }
            
            responseString = new String(response, "ASCII");

            if(responseString.equals("ER\r\n"))
            {
            	throw new Exception("An error occurred on the other side.");
            }
            else if(responseString.equals("OK\r\n"))
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
            	handleResponse(responseString);
            	sock.close();
            }
            catch(Exception e)
            {	
            }
        }
	}
	
	public void handleResponse(String responseString)
	{
		 System.out.println("SearchCmd response: " + responseString);
	}
	
	public Socket getSocket()
	{
		return sock;
	}
}
