package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import peerclient.PeerClient;
import peerclient.commands.PeerSearchResult;

import communication.Connection;

import data.ClientInfo;

public class SearchPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private PeerClient client;

	private JTextField searchField;
	private JTable resultsTable;

	private AbstractTableModel resultsModel = new AbstractTableModel() {

		private static final long serialVersionUID = 1L;
		private List<PeerSearchResult> results = new LinkedList<PeerSearchResult>();
		private String[] columnNames = { "Name", "Size", "Hash" };

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			int rv;
			synchronized (results) {
				rv = results.size();
			}
			return rv;
		}

		@Override
		public String getColumnName(int col) {
			return columnNames[col];
		}

		@Override
		public Object getValueAt(int row, int col) {
			PeerSearchResult result;
			synchronized (results) {
				result = results.get(row);
			}
			switch (col) {
			case 0:
				return result.getName();
			case 1:
				return result.getSize();
			case 2:
				return result.getHash();
			case 3:
				return result;
			}
			return "ERROR";
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			if (value == null) {
				results.remove(row);
				return;
			}
			
			if (!(value instanceof PeerSearchResult))
				return;

			synchronized (results) {
				results.add((PeerSearchResult) value);
			}
			fireTableCellUpdated(row, col);
		}
	};

	public SearchPanel(PeerClient client) {
		super(new BorderLayout());
		this.add(getInputPanel(), BorderLayout.PAGE_START);

		this.client = client;

		resultsTable = getResultsTable();
		this.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

		addResults(Arrays.asList(new PeerSearchResult("", 0, "test", 1, "hash?")));
		
		//clearResults();
		
		//addResults(Arrays.asList(new SearchResult(new File("client.conf"),
		//		"hash"), new SearchResult(new File("README"), "READMEhash")));
		
	}

	private JPanel getInputPanel() {

		ActionListener searchListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("Searching for: " + searchField.getText());

				Connection conn = new Connection();
				String searchString = searchField.getText();
				addResults(Arrays.asList(new PeerSearchResult("", 0, searchString, 1, "hash?")));

				try {
					ArrayList<ClientInfo> peerList = conn.listPeers();
					client.searchAllPeers(peerList, searchString);
				} catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
		};

		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));
		searchField = new JTextField();
		JButton searchButton = new JButton("Search");

		searchField.addActionListener(searchListener);
		searchButton.addActionListener(searchListener);

		inputPanel.add(searchField);
		inputPanel.add(searchButton);

		return inputPanel;
	}

	private JTable getResultsTable() {

		final JTable resultsTable = new JTable(resultsModel);
		resultsTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = resultsTable.getSelectedRow();
					PeerSearchResult s = (PeerSearchResult) resultsModel.getValueAt(row, 3);
					ClientUI.getInstance().peerClient.downloadFileFromPeer(
							s.getIP(), s.getPort(), s.getHash(), s.getName(), Integer.parseInt(s.getSize()));
					System.out.println("Clicked "+s.getName());
				}
			}
		});

		return resultsTable;
	}

	public void addResults(Iterable<PeerSearchResult> results) {
		for (PeerSearchResult result : results) {
			resultsTable.setValueAt(result, resultsTable.getRowCount(), 0);
			resultsModel.fireTableDataChanged();
		}
	}
	
	public void clearResults() {
		while (resultsModel.getRowCount() > 0) {
			System.out.println("Removing");
			resultsModel.setValueAt(null, 0, 0);
		}
		resultsModel.fireTableDataChanged();
	}
}
