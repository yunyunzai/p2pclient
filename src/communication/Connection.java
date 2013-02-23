package communication;

import java.io.InputStream;
import java.io.OutputStream;

import java.net.ServerSocket;
import java.net.Socket;

public class Connection 
{	
	//Used to create the listening connection
	private ServerSocket peerServer;
	private Socket peerSocket;
	
	public Connection()
	{		  
		peerServer = null;  
		peerSocket = null;
	}
	
	public void connectServer() throws Exception
	{
		try
		{
			sendCommand("CONNECT 2112\r\n\r\n");
		}
		catch(Exception e)
		{
			throw new Exception("Could not connect to the server: " + e.getMessage()); 
		}
		
		//Create listening connection
    	createListeningConnection(2112);
	}
	
	private void createListeningConnection(int port)
	{
		//TODO: create a listening connection for peers after telling the server you're alive
	}

	public void disconnectServer() throws Exception
	{
		
	}
	
	private void sendCommand(String cmd) throws Exception
	{
		Socket s = null;
		InputStream in = null;
		OutputStream out = null;
		try
		{            
            //Create the socket with the server
			//TODO: Maybe put the IP and port in a separate file and read from it so we have a configurable client
			s = new Socket("128.189.161.17", 2111);  
            
            //Get the data output stream  
			out = s.getOutputStream();  
              
            //Send a command to the server
            //TODO: Maybe put all the commands in a separate class so we can have more control of them
            out.write(cmd.getBytes("ASCII"));            
            out.flush();
            
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
            	
            	bytesRead += in.read(response, bytesRead, 100);
            }
            
            String responseString = new String(response, "ASCII");
            
            //TODO: check why the heck the response is not matching any of both
            if(responseString == "ER\r\n")
            {
            	throw new Exception("An error occurred on the other side.");
            }
            else if(responseString != "OK\r\n")
            {
            	throw new Exception("The other side didn't respond properly.");
            }
        }
		catch(Exception e)
		{              
            throw new Exception("Could not send command: " + e.getMessage());         
        }
		finally
		{              
            try
            {
            	//Close the connection with server
            	in.close();
            	out.close();
                s.close();
            }
            catch(Exception e)
            {	
            }
        }
	}
}
