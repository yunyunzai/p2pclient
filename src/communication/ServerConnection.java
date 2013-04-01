package communication;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import settings.Settings;
import util.Network;

public class ServerConnection {
	protected static final String RESPONSE_OK = "OK\r\n";
	
	protected String cmd;
	protected Socket sock;
	
	public void send() throws Exception
	{
		sock = null;
		//InputStream in = null;
		OutputStream out = null;
		String responseString = null;
		
		try
		{            
			sock = new Socket(Settings.SERVER_IP, Settings.SERVER_PORT);
            
            //Get the data output stream  
			out = sock.getOutputStream();  
              
            //Send a cmd to the server
            //TODO: Maybe put all the cmds in a separate class so we can have more control of them
            out.write(cmd.getBytes("ASCII"));            
            out.flush();
            
            //Get the response input stream
            //in = sock.getInputStream();
            //TODO: make size of response configurable as well
            //byte[] response = new byte[100];
            
            //int bytesRead = in.read(response);
            //TODO: make the size of the response as a constant
            //while(bytesRead < 3)
            //{
            //	if(bytesRead == -1)
            //    {
            //    	throw new Exception("Connection closed by the other side.");
            //    }
            	
            //	bytesRead += in.read(response, bytesRead, 100);
            //}
            //responseString = new String(response, "ASCII");
            responseString = Network.readMessage(sock);
            System.out.println(responseString);
            //TODO: check why the heck the response is not matching any of both
            if(responseString.equals("ER\r\n"))
            {
            	throw new Exception("An error occurred on the other side.");
            }
            else if(responseString.equals("OK\r\n"))
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
//            	in.close();
//            	out.close();
//                s.close();
            	handleResponse(responseString);
            	sock.close();
            }
            catch(Exception e)
            {	
            }
        }
	}
	
	protected void handleResponse(String response)
	{
		
	}
	
}
