package peerclient.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import settings.Settings;
import ui.ClientUI;

public class DownloadCmd implements Runnable 
{
	protected Socket sock;
	private boolean exit = false;
	private String ip, fileHash, fileName;
	private int port, fileSizeBytes;
	private int bytesReceived, bytesPreviouslyReceived;
	private Date previousTime;
	private String speed;
	private HashSet<Integer> unreceivedSeqs;
	
	private int currentSeq=0;

	public DownloadCmd(String ip, int port, String fileHash, String fileName, int fileSizeBytes)
	{
		this.ip = ip;
		this.port = port;
		this.fileHash = fileHash;
		this.fileName = fileName;
		this.fileSizeBytes = fileSizeBytes;

		this.speed = "0 B/s";
		this.bytesReceived = 0;
		this.bytesPreviouslyReceived = 0;
		this.previousTime = new Date();

		synchronized(ClientUI.getInstance().panelDownload.downloads)
		{
			ClientUI.getInstance().panelDownload.downloads.add(this);
		}	
	}

	public String getSource()
	{
		return ip + ":" + port;
	}

	public String getFileName()
	{
		return fileName;
	}

	private String getSizeFormated(float sizeBytes)
	{
		float returnSize = sizeBytes;
		//Return in bytes
		String returnLabel = " B";
		if(returnSize > 1024)
		{
			//Return in Kbytes
			returnLabel = " KB";
			returnSize = returnSize / 1024;
			if(returnSize > 1024)
			{
				//Return in Mbytes
				returnLabel = " MB";
				returnSize = returnSize / 1024;
				if(returnSize > 1024)
				{
					returnSize = returnSize / 1024;
					//Return in Gbytes
					returnLabel = " GB";
				}
			}
		}		
		return new DecimalFormat("#.##").format(returnSize) + returnLabel;
	}

	public String getFileSize()
	{
		return this.getSizeFormated(fileSizeBytes);
	}

	public String getDownloaded()
	{
		return this.getSizeFormated(bytesReceived);
	}

	public void updateSpeed()
	{
		Date now = new Date();
		long secondsPassed = (now.getTime() - previousTime.getTime()) / 1000;
		long amountDownloaded = bytesReceived - bytesPreviouslyReceived;

		previousTime = now;
		bytesPreviouslyReceived = bytesReceived;

		if(secondsPassed == 0)
		{
			return;
		}

		this.speed = this.getSizeFormated(amountDownloaded / secondsPassed) + "/s";
	}

	public String getSpeed()
	{
		return speed;
	}

	public float getProgress()
	{
		return bytesReceived / fileSizeBytes;
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
		bytesReceived = 0;

		
		
		
		
		
		try 
		{	
			unreceivedSeqs = readLogFile(fileHash);
			
			
			// create the dummy file first
			destFile = new File(Settings.SHARED_FOLDER + "/" + fileName);
			RandomAccessFile f = new RandomAccessFile(destFile, "rw");
			f.setLength(fileSizeBytes);
			f.close();
//			while (currentSeq*Settings.CHUNK_SIZE<fileSizeBytes)
			while (unreceivedSeqs.size()!=0)
			{
				currentSeq=(int)unreceivedSeqs.toArray()[0];
				String cmd = "DOWNLOAD " + fileHash + " "+currentSeq+"\r\n";
				System.out.println(currentSeq+" "+fileSizeBytes);
				sock = new Socket(ip, port);
				cmdOutput = sock.getOutputStream();
				cmdOutput.write(cmd.getBytes("ASCII"));            
				cmdOutput.flush();

				fileInput = new BufferedInputStream(sock.getInputStream());
				destFile = new File(Settings.SHARED_FOLDER + "/" + fileName);
				RandomAccessFile rfile=new RandomAccessFile(destFile,"rw");
				fileOutput = new BufferedOutputStream(new FileOutputStream(rfile.getFD()));
				
				int i;
//				byte[] hashRead=new byte[Settings.HASH_SIZE];
//				fileInput.read(hashRead);
//				System.out.println("hash: "+hashRead);
				byte[] chunkRead=new byte[Settings.CHUNK_SIZE];				
				if ((i = fileInput.read(chunkRead)) != -1)
				{
					if(exit)
					{            		
					}
					//            	fileOutput.write(i);
					
					rfile.seek(currentSeq*Settings.CHUNK_SIZE);
//					rfile.write(chunkRead, 0, i);
					
					fileOutput.write(chunkRead, 0, i);
					fileInput.close();
					fileOutput.close();
					rfile.close();
					logProgress(fileHash,currentSeq);
					bytesReceived++;
				}
				//currentSeq++;
				
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

//					if(bytesReceived != fileSizeBytes)
//					{
//						//It's not the file we wanted, or at least not with the same size. Let's delete it
//						if(destFile.exists())
//						{
//							destFile.delete();
//						}
//						throw new Exception("The size of the file downloaded doesn't match with the size of the file requested.");
//					}
//					else
//					{	
//					String hash = Files.hash(destFile, Hashing.sha1()).toString();
//					if(!new String(hashRead).equals(hash))
//					{
//						//It's not the file we wanted, or at least not with the same hash. Let's delete it
//						if(destFile.exists())
//						{
//							destFile.delete();
//						}
//						throw new Exception("The hash of the file downloaded doesn't match with the hash of the file requested.");
//					}
//					}
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
//				fileInput.close();
//				fileOutput.close();
				sock.close();
			}
			catch(Exception e){}				
		}

		synchronized(ClientUI.getInstance().panelDownload.downloads)
		{
			//ClientUI.getInstance().panelDownload.downloads.remove(this);
		}				
	}

	public Socket getSocket()
	{
		return sock;
	}
	
	private HashSet<Integer> readLogFile(String fileHash)
	{
		File logFile=new File(Settings.SHARED_FOLDER+"/"+fileHash+".tmp");
		HashSet<Integer> result=new HashSet<Integer>();
		try {
			
				RandomAccessFile f=new RandomAccessFile(logFile,"rw");
				if (logFile.length()==0)
				{
					System.out.println("Creating log file!!");
					for (int i=0;i<=fileSizeBytes/Settings.CHUNK_SIZE;i++)
					{
						
						//String s=i+" ";
						f.writeInt(i);
						result.add(i);
					}
				}
				else
				{
					int i;
					while ((i=f.readInt())!=-1)
						result.add(i);					
				}
				f.close();			
			
		} 
		catch (EOFException e) {
			return result;		
		}		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		return result;
	}
	
	private void logProgress(String fileHash,int seq)
	{
		File logFile=new File(Settings.SHARED_FOLDER+"/"+fileHash+".tmp");
		logFile.delete();
		logFile=new File(Settings.SHARED_FOLDER+"/"+fileHash+".tmp");
		try {
			RandomAccessFile f=new RandomAccessFile(logFile,"rw");
			this.unreceivedSeqs.remove(seq);
			for (int i:unreceivedSeqs)
			{
				f.writeInt(i);
			}
			f.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
