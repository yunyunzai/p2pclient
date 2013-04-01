package util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Network {

	private static final int BUFFER_SIZE = 100;
	
	public static String readMessage(Socket s) throws IOException {
		byte[] buf = new byte[BUFFER_SIZE];
		String msg = "";
		int bytesRead = 0;
		int rv;
		
		InputStream in = s.getInputStream();
		
		while (true) {
			
			if ((rv = in.read(buf, 0, BUFFER_SIZE)) == -1) {
				return msg;
			}
			
			msg += new String(buf, 0, rv);
			if (msg.indexOf("\r\n", (bytesRead < 2) ? 0 : bytesRead-2) != -1) {
				return msg;
			}
		
			bytesRead += rv;
		}
	}
	
	public static void main(String[] args) throws IOException {
		Socket s = new Socket();
		s.connect(new InetSocketAddress("localhost", 6666));
		s.getOutputStream().write("CONNECT 1220\r\n\r\n".getBytes());
		System.out.println(readMessage(s));
	}
}
