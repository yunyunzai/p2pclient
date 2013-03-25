package peerclient.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import settings.Settings;

public class DownloadCmd implements Runnable {
	protected Socket sock;
	boolean exit = false;
	private String ip, fileHash, fileName;
	private int port, fileSizeBytes;
	
	public DownloadCmd(String ip, int port, String fileHash, String fileName, int fileSizeBytes)
	{
		this.ip = ip;
		this.port = port;
		this.fileHash = fileHash;
		this.fileName = fileName;
		this.fileSizeBytes = fileSizeBytes;
	}
	
	public void run() 
	{	
		sock = null;
		OutputStream cmdOutput = null;
		BufferedInputStream fileInput = null;
		BufferedOutputStream fileOutput = null;
		File destFile = null;
		int bytesReceived = 0;
		
		try 
		{
			String cmd = "DOWNLOAD " + fileHash;
			sock = new Socket(ip, port);
			cmdOutput = sock.getOutputStream();
			cmdOutput.write(cmd.getBytes("ASCII"));            
			cmdOutput.flush();
			
			fileInput = new BufferedInputStream(sock.getInputStream());
			destFile = new File(Settings.SHARED_FOLDER + "/" + fileName);
	        fileOutput = new BufferedOutputStream(new FileOutputStream(destFile));
	        
	        int i;
            while ((i = fileInput.read()) != -1)
            {
            	if(exit)
            	{
            		//TODO: Stop downloading
            	}
            	fileOutput.write(i);
            	bytesReceived++;
            }
            fileOutput.flush();
            
            if(bytesReceived != fileSizeBytes)
            {
            	//TODO: Size of file downloaded is not the same as the metadata
            }
            else
            {
            	//TODO: Calculate hash of new file?
            }
		} 
		catch (Exception e) 
		{
			//TODO: Report error to the UI
		}
		finally
		{
			try
			{
				fileInput.close();
				fileOutput.close();
				sock.close();
			}
            catch(Exception e){}				
		}
				
	}
	
	public void handleResponse(String responeString)
	{
		
	}
	
	public Socket getSocket()
	{
		return sock;
	}
}
