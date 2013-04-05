package peerclient.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import settings.Settings;
import ui.ClientUI;
import ui.P2PClient;

public class DownloadCmd implements Runnable 
{
	protected Socket sock;
	private boolean exit = false;
	private String ip, fileHash, fileName;
	private int port, fileSizeBytes;
	private int bytesReceived, bytesPreviouslyReceived;
	private Date previousTime;
	private String speed;
	//private HashSet<Integer> unreceivedSeqs;

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
			for (DownloadCmd d:ClientUI.getInstance().panelDownload.downloads)
			{
				if (d.fileHash.equals(this.fileHash))
				{
					//ClientUI.getInstance().panelDownload.downloads.remove(d);

				}
			}
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

	synchronized public void run() 
	{	
		sock = null;
		OutputStream cmdOutput = null;
		BufferedInputStream fileInput = null;
		BufferedOutputStream fileOutput = null;
		File destFile = null;
		bytesReceived = 0;

		RandomAccessFile rfile=null;



		try 
		{				

			// create the dummy file first
			destFile = new File(Settings.SHARED_FOLDER + "/" + fileName);

			// if the file already exists and finished downloading already don't need to download anything
			if (destFile.exists())
			{
				if (Files.hash(destFile, Hashing.sha1()).toString().equals(fileHash))
				{
					synchronized(ClientUI.getInstance().panelDownload.downloads)
					{
						ClientUI.getInstance().panelDownload.downloads.remove(this);
					}
					return;
				}

			}			
			RandomAccessFile f = new RandomAccessFile(destFile, "rw");
			f.setLength(fileSizeBytes);
			f.close();
			
			// set initial unreceived sequeces to the last state remembered
			synchronized (Settings.unreceivedSeqs){

				if (Settings.unreceivedSeqs.get(this.fileHash)==null||Settings.unreceivedSeqs.get(this.fileHash).size()==0)
					Settings.unreceivedSeqs.put(this.fileHash, readLogFile(fileHash));
			}

			
			// number of unreceived sequeces
			int size;
			synchronized (Settings.unreceivedSeqs){
				size=Settings.unreceivedSeqs.get(this.fileHash).size();
			}
			
			while (size!=0 && !exit)
			{
				System.out.println("received bytes: "+bytesReceived);
				System.out.println("chunks remaining: "+Settings.unreceivedSeqs.get(this.fileHash).size());
				synchronized (Settings.unreceivedSeqs){
					if (Settings.unreceivedSeqs.get(this.fileHash).toArray().length<=0)
						break;
					currentSeq=(Integer)Settings.unreceivedSeqs.get(this.fileHash).toArray()[new Random().nextInt(Settings.unreceivedSeqs.get(this.fileHash).toArray().length)];
					System.out.println(currentSeq+" "+fileSizeBytes);
				}
				String cmd = "DOWNLOAD " + fileHash + " "+currentSeq+"\r\n";

				sock = new Socket(ip, port);
				cmdOutput = sock.getOutputStream();
				cmdOutput.write(cmd.getBytes("ASCII"));            
				cmdOutput.flush();

				fileInput = new BufferedInputStream(sock.getInputStream());
				destFile = new File(Settings.SHARED_FOLDER + "/" + fileName);
				rfile=new RandomAccessFile(destFile,"rw");
				fileOutput = new BufferedOutputStream(new FileOutputStream(rfile.getFD()));

				int i;
				byte[] chunkRead=new byte[Settings.CHUNK_SIZE];				
				if ((i = fileInput.read(chunkRead)) != -1)
				{
					rfile.seek(currentSeq*Settings.CHUNK_SIZE);

					synchronized (Settings.unreceivedSeqs){
						fileOutput.write(chunkRead, 0, i);
						fileOutput.flush();
						fileInput.close();
						fileOutput.close();
						rfile.close();
					}
					bytesReceived+=i;
					logProgress(fileHash,currentSeq);

				}
				//currentSeq++;
				synchronized (Settings.unreceivedSeqs){
					size=Settings.unreceivedSeqs.get(this.fileHash).size();
				}

				//fileOutput.flush();

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
				// close everything if not closed
				fileInput.close();
				fileOutput.close();
				rfile.close();

			}
			// when download finished check if the downloaded file is the same
			//Thread.sleep(10000);
			if (!exit)
			{
				String hash = Files.hash(destFile, Hashing.sha1()).toString();
				if(!fileHash.equals(hash))
				{
					//It's not the file we wanted, or at least not with the same hash. Let's delete it
					if(destFile.exists())
					{
						destFile.delete();
					}
					this.deleteLogFile(fileHash);
					throw new Exception("The hash of the file downloaded doesn't match with the hash of the file requested.");
				}
				deleteLogFile(fileHash);
				synchronized(ClientUI.getInstance().panelDownload.downloads)
				{
					//					ClientUI.getInstance().panelDownload.downloads.remove(this);
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			
			//TODO: Report error to the UI
		}
		finally
		{
			try
			{ // close everything if not closed
				
				if (fileInput!=null)
					fileInput.close();
				if (fileOutput!=null)
					fileOutput.close();
				if (rfile!=null)
					rfile.close();
				if (sock!=null)
					sock.close();				
			}
			catch(Exception e){e.printStackTrace();}				
		}


	}

	public Socket getSocket()
	{
		return sock;
	}


	private HashSet<Integer> readLogFile(String fileHash) throws IOException
	{
		File logFile=new File(Settings.SHARED_FOLDER+"/"+fileHash+".tmp");
		HashSet<Integer> result=new HashSet<Integer>();
		RandomAccessFile f=null;
		try {			
			f=new RandomAccessFile(logFile,"rw");
			if (logFile.length()==0)
			{
				System.out.println("Creating log file!!");
				f.writeInt(0);
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
				bytesReceived=f.readInt();
				while ((i=f.readInt())!=-1)
					result.add(i);					
			}
			f.close();

		} 
		catch (EOFException e) {				
			f.close();			
			return result;		
		}		
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}

		return result;
	}

	synchronized private void logProgress(String fileHash,int seq)
	{
		File logFile=new File(Settings.SHARED_FOLDER+"/"+fileHash+".tmp");

		//System.out.println("DELETING old log file "+logFile.delete());
		//logFile=new File(Settings.SHARED_FOLDER+"/"+fileHash+".tmp");
		try {
			RandomAccessFile f=new RandomAccessFile(logFile,"rw");
			synchronized (Settings.unreceivedSeqs){
				Settings.unreceivedSeqs.get(this.fileHash).remove(seq);

				f.writeInt(bytesReceived);
				for (int i:Settings.unreceivedSeqs.get(this.fileHash))
				{
					f.writeInt(i);
				}
			}
			f.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private void deleteLogFile(String fileHash)
	{
		File logFile=new File(Settings.SHARED_FOLDER+"/"+fileHash+".tmp");

		//logFile.deleteOnExit();
		System.out.println("DOWNLOAD successful, DELETING log file~~ "+logFile.delete());
	}

	public void pauseResumeDownload()
	{
		try {
			if (!exit)
			{
				exit=true;
				this.sock.close();	
			}
			else
			{		
				exit=false;
				ClientUI.getInstance().peerClient.downloadFileFromPeer(ip, port, fileHash, fileName, fileSizeBytes);
			}			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
