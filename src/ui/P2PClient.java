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

public class P2PClient {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
	    
	    try {
            Settings.loadSettings();
            LocalShares.buildIndex();
        } catch (FileNotFoundException e1) {
            System.out.println("ERROR: " + Settings.CONF_FILE + " not found");
        } catch (InvalidRCException e1) {
            System.out.println("ERROR: failed to parse " + Settings.CONF_FILE);
        }
	    
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientUI.getInstance().initialize();
					ClientUI.getInstance().frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
