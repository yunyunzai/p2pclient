package ui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;

import communication.Connection;

import java.awt.BorderLayout;

public class ClientUI {

	public static JFrame frame;
	private JMenuItem mntmConnect;
	private JMenuItem mntmList;
	private JMenuItem mntmDisconnect;
	private JTextArea txtLog;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
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
				
				
				//Handle UI stuff
				mntmConnect.setEnabled(true);
				mntmDisconnect.setEnabled(false);
			}
		});
		mntmDisconnect.setEnabled(false);
		mnFile.add(mntmDisconnect);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnFile.add(mntmExit);
		
		txtLog = new JTextArea();
		txtLog.setEditable(false);
		frame.getContentPane().add(txtLog, BorderLayout.CENTER);
	}
	
	private void connect_server()
	{
		//Connect to the server
		txtLog.setText(txtLog.getText() + "\nConnecting to the server...");
		
		Connection conn = new Connection();
		try
		{
			conn.connectServer();
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
