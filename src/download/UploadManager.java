package download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import search.ChunkedFile;
import search.LocalShares;
import settings.Settings;

public class UploadManager 
{
	private Socket sock;
	private ChunkedFile file;
	private UploadThread uploadThread;
	private long seqNum;
	private String chunkHash;
	
	public UploadManager(Socket sock, String fileHash, long seq)
	{
		this.sock = sock;
		this.file = LocalShares.getFile(fileHash);
		this.seqNum=seq;
		this.chunkHash= this.file.getChunkHash((int)seqNum);
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
				RandomAccessFile chunkFile=new RandomAccessFile(file,"r");
				chunkFile.seek(seqNum*Settings.CHUNK_SIZE);
				byte[] chunkToUpload=new byte[Settings.CHUNK_SIZE];
				chunkFile.read(chunkToUpload,0,Settings.CHUNK_SIZE);
				
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
