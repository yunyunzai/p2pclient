package peerclient.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
			String cmd = "DOWNLOAD " + fileHash + "\r\n";
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
	            
	            if(bytesReceived < 10)
	            {	            	
	            	//Check if it's not an error message
	            	char msg[] = new char[10];
	            	FileReader reader = new FileReader(destFile);
	            	reader.read(msg);
	            	reader.close();
	            	String response = new String(msg);
	            	if(response.equals("ER\r\n"))
	                {
	            		//It's an error message, it means the server doesn't have the file
	            		throw new Exception("The other side replied saying it doesn't have the requested file.");
	                }
	            }
	            
	            if(bytesReceived != fileSizeBytes)
	            {
	            	//It's not the file we wanted, or at least not with the same size. Let's delete it
	            	if(destFile.exists())
	            	{
	            		destFile.delete();
	            	}
	            	throw new Exception("The size of the file downloaded doesn't match with the size of the file requested.");
	            }
	            else
	            {	
	            	String hash = Files.hash(destFile, Hashing.sha1()).toString();
	            	if(!fileHash.equals(hash))
	            	{
	            		//It's not the file we wanted, or at least not with the same hash. Let's delete it
		            	if(destFile.exists())
		            	{
		            		destFile.delete();
		            	}
		            	throw new Exception("The hash of the file downloaded doesn't match with the hash of the file requested.");
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
				//TODO: delete the file if it hasn't been downloaded successfully
				fileInput.close();
				fileOutput.close();
				sock.close();
			}
            catch(Exception e){}				
		}
				
	}
	
	public Socket getSocket()
	{
		return sock;
	}
}
