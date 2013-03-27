package ui;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;

import search.LocalShares;
import settings.InvalidRCException;
import settings.Settings;

import communication.Connection;
import communication.PeerServer;

import java.awt.BorderLayout;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import javax.swing.JTabbedPane;

import data.ClientInfo;

import peerclient.PeerClient;

public class ClientUI {
	private static ClientUI instance = null;

	public static JFrame frame;
	private JMenuItem mntmConnect;
	private JMenuItem mntmList;
	private JMenuItem mntmDisconnect;
	private JTextArea txtLog;
	PeerServer peerServer;
	PeerClient peerClient;
	Connection conn;
	private JTabbedPane tabbedPane;

	
	/**
	 * Create the application.
	 */
	protected ClientUI() 
	{
		
	}
	
	public static ClientUI getInstance()
	{
		if (instance == null)
		{
			instance = new ClientUI();
		}
		
		return instance;
	}
	
	public void initialize()
	{
		initializePeerServer();
		initializePeerClient();
		initializeUI();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initializeUI() {
		frame = new JFrame();
		frame.setBounds(20, 20, 640, 480);
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
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		// search tab
		
		/*JPanel panelSearch = new JPanel(false);
		// search text field
		final JTextField searchField = new JTextField(30);
		panelSearch.add(searchField);
		// search button
		JButton searchButton = new JButton("SEARCH");
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				String searchString = searchField.getText();
				
				try {
					System.out.println("HELLO");
					ArrayList<ClientInfo> peerList = conn.listPeers();
					peerClient.searchAllPeers(peerList, searchString);
				} catch (Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}
		});
		panelSearch.add(searchButton);
		
		tabbedPane.addTab("Search", panelSearch);*/
		tabbedPane.addTab("Search", new SearchPanel(peerClient));
		JPanel panelDownload = new JPanel(false);
		tabbedPane.addTab("Download", panelDownload);
		JPanel panelLog = new JPanel(false);
		tabbedPane.addTab("Log", panelLog);
		
		txtLog = new JTextArea();
		txtLog.setTabSize(100);
		panelLog.add(txtLog);
		txtLog.setEditable(false);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
				
	}
	private void initializePeerServer()
	{
		peerServer=new PeerServer();
		start_peer_server();
	}
	
	private void initializePeerClient()
	{
		peerClient = new PeerClient();
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
