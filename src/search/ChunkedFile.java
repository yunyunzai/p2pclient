package search;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import settings.Settings;

public class ChunkedFile extends File {

	private static final long serialVersionUID = 1L;
	
	private String[] hashes;
	
	public ChunkedFile(File file) throws IOException, NoSuchAlgorithmException {
		super(file.getPath());
		
		if (!this.isFile() || !this.canRead())
			throw new IOException(file.getPath()+" is not a chunkable file");
		
		hashes = new String[(int)(this.length() / Settings.CHUNK_SIZE)+1];
		
		MessageDigest md = MessageDigest.getInstance("SHA1");
		InputStream in = new FileInputStream(this);
		byte[] dest = new byte[Settings.CHUNK_SIZE];
		
		try {
			System.out.println("Hashing "+this.length()+" bytes");
			in = new DigestInputStream(in, md);
			for (int i = 0; i < hashes.length; i++) {
				if (in.read(dest, 0, Settings.CHUNK_SIZE) == -1)
					break;
				
				byte[] digest = md.digest();
				hashes[i] = digest.toString();
				System.out.println("Hash "+i+" is "+hashes[i]);
			}
		} finally {
			in.close();
		}
	}
	
	public int getNumChunks() {
		return hashes.length;
	}
	
	public String getChunkHash(int num) {
		return hashes[num];
	}
}