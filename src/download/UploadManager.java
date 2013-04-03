package download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;

public class UploadManager 
{
	private Socket sock;
	private File file;
	private UploadThread uploadThread;
	
	public UploadManager(Socket sock, File fileToUpload, long seq)
	{
		this.sock = sock;
		this.file = fileToUpload;
		this.uploadThread = null;
	}
	
	public void start_upload()
	{
		this.uploadThread = new UploadThread();
		this.uploadThread.start();
	}
	
	private class UploadThread extends Thread
	{
		boolean exit = false;
		
		@Override
		public void run() 
		{
			BufferedInputStream fileInput = null;
			BufferedOutputStream fileOutput = null;
			
			try
			{
				fileInput = new BufferedInputStream(new FileInputStream(file));
				fileOutput = new BufferedOutputStream(sock.getOutputStream());
				
				int i;
	            while ((i = fileInput.read()) != -1)
	            {
	            	if(exit)
	            	{
	            		//TODO: Stop uploading
	            	}
	            	fileOutput.write(i);	            	
	            }
	            fileOutput.flush();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					fileInput.close();
					fileOutput.close();
					sock.close();
				}
	            catch(Exception e){ e.printStackTrace(); }
			}
		}
		
		public void exit()
		{
			exit = true;
		}
	}
}
