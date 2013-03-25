package peerclient.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

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
	
	public void exit()
	{
		exit = true;
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
            		break;
            	}
            	fileOutput.write(i);
            	bytesReceived++;
            }
            
            if(exit)
            {
            	//We don't want to download anymore, so delete the file we were creating
            	if(destFile.exists())
            	{
            		destFile.delete();
            	}
            }
            else
            {
	            fileOutput.flush();
	            
	            if(bytesReceived != fileSizeBytes)
	            {
	            	//TODO: Size of file downloaded is not the same as the metadata, what do we do?
	            	//Throw exception?
	            }
	            else
	            {
	            	//TODO: Calculate hash of new file?
	            	String hash = Files.hash(destFile, Hashing.sha1()).toString();
	            	if(!fileHash.equals(hash))
	            	{
	            		//TODO: The hash doesn't match, what do we do?
	            		//Throw exception?
	            	}
	            }
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
	
//	public void handleResponse(String responeString)
//	{
//		
//	}
	
	public Socket getSocket()
	{
		return sock;
	}
}
