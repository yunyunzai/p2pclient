package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import peerclient.PeerClient;

import communication.Connection;

import data.ClientInfo;

import search.SearchResult;

public class SearchPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;

    private PeerClient client;
    
    private JTextField searchField;
    private JTable resultsTable;
    
    public SearchPanel(PeerClient client) {
        super(new BorderLayout());
        this.add(getInputPanel(), BorderLayout.PAGE_START);
        
        this.client = client;
        
        resultsTable = getResultsTable();
        this.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        
        resultsTable.setValueAt(new SearchResult(new File("client.conf"), "testhash"), 0, 0);
        
    }
    
    private JPanel getInputPanel() {
        
        ActionListener searchListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.out.println("Searching for: "+searchField.getText());
                
                Connection conn = new Connection();
                String searchString = searchField.getText();
                
                try {
                    System.out.println("HELLO");
                    ArrayList<ClientInfo> peerList = conn.listPeers();
                    client.searchAllPeers(peerList, searchString);
                } catch (Exception ex)
                {
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
        
        TableModel resultsModel = new AbstractTableModel() {
            private static final long serialVersionUID = 1L;
            private List<SearchResult> results = new LinkedList<SearchResult>();
            private String[] columnNames = { "Name", "Size", "Hash" };
            
            @Override
            public int getColumnCount() {
                return 3;
            }
            @Override
            public int getRowCount() {
                return results.size();
            }
            @Override
            public String getColumnName(int col) {
                return columnNames[col];
            }
            @Override
            public Object getValueAt(int row, int col) {
                SearchResult result = results.get(row);
                switch (col) {
                case 0:
                    return result.getName();
                case 1:
                    return result.getSize();
                case 2:
                    return result.getHash();
                }
                return "ERROR";
            }
            @Override
            public void setValueAt(Object value, int row, int col) {
                if (!(value instanceof SearchResult))
                    return;
                
                results.add((SearchResult) value);
                fireTableCellUpdated(row, col);
            }
        };
        
        JTable resultsTable = new JTable(resultsModel);
        
        return resultsTable;
    }
}
