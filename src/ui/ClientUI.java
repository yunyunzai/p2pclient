package ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;

import settings.InvalidRCException;
import settings.Settings;

import communication.Connection;
import communication.PeerServer;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;

public class ClientUI {

	public static JFrame frame;
	private JMenuItem mntmConnect;
	private JMenuItem mntmList;
	private JMenuItem mntmDisconnect;
	private JTextArea txtLog;
	PeerServer peerServer;
	Connection conn;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
	    
	    try {
            Settings.loadSettings();
        } catch (FileNotFoundException e1) {
            System.out.println("ERROR: " + Settings.CONF_FILE + " not found");
        } catch (InvalidRCException e1) {
            System.out.println("ERROR: failed to parse " + Settings.CONF_FILE);
        }
	    
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientUI window = new ClientUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientUI() {
		initializeUI();
		initializePeerServer();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initializeUI() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmConnect = new JMenuItem("Connect");
		mntmConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				connect_server();
			}
		});
		mnFile.add(mntmConnect);
		
		mntmList = new JMenuItem("List");
		mntmList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				list_peers();
			}
			
		});
		mnFile.add(mntmList);
		
		mntmDisconnect = new JMenuItem("Disconnect");
		mntmDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				//Disconnect from server
				// TODO implement disconnect function				
				
				
				//Handle UI stuff
				mntmConnect.setEnabled(true);
				mntmDisconnect.setEnabled(false);
			}
		});
		mntmDisconnect.setEnabled(false);
		mnFile.add(mntmDisconnect);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{

				//Stop the peer server
				peerServer.closePeerServer();
				
				frame.dispose();
			}
		});
		mnFile.add(mntmExit);
		
		txtLog = new JTextArea();
		txtLog.setEditable(false);
		frame.getContentPane().add(txtLog, BorderLayout.CENTER);
		
				
	}
	private void initializePeerServer()
	{
		peerServer=new PeerServer();
		start_peer_server();
	}
	private void start_peer_server()
	{
		peerServer.startPeerServer();
	}
	
	private void connect_server()
	{
		//Connect to the server
		txtLog.setText(txtLog.getText() + "\nConnecting to the server...");
		
		conn = new Connection();
		try
		{
			conn.connectServer();
			start_peer_server();
		}
		catch(Exception e)
		{
			txtLog.setText(txtLog.getText() + "\n" + e.getMessage());
			return;
		}
		
		//Handle UI stuff
		mntmConnect.setEnabled(false);
		mntmDisconnect.setEnabled(true);
	}
	
	private void list_peers() {
		//Getting peer table
		txtLog.setText(txtLog.getText() + "\nGetting peer table...");
		Connection conn = new Connection();
		try
		{
			conn.listPeers();
		}
		catch(Exception e)
		{
			txtLog.setText(txtLog.getText() + "\n" + e.getMessage());
			return;
		}
	}
}
