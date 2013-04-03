package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import peerclient.commands.DownloadCmd;

public class DownloadPanel extends JPanel 
{	
	private static final long serialVersionUID = 1L;
//	private class ProgressCellRender extends JProgressBar implements TableCellRenderer {
//
//        /**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//
//		@Override
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            int progress = 0;
//            if (value instanceof Float) {
//                progress = Math.round(((Float) value) * 100f);
//            } else if (value instanceof Integer) {
//                progress = (Integer) value;
//            }
//            setValue(progress);
//            return this;
//        }
//    }
	
	public List<DownloadCmd> downloads;
	
	public AbstractTableModel dowloadTableModel = new AbstractTableModel() {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private String[] columnNames = { "Source", "Name", "Size", "Downloaded", "Speed"};
		
		@Override
		public Object getValueAt(int row, int col) 
		{
			DownloadCmd download;
			synchronized (downloads) 
			{
				download = downloads.get(row);
			}
			switch (col) 
			{
				case 0:
					return download.getSource();
				case 1:
					return download.getFileName();
				case 2:
					return download.getFileSize();
				case 3:
					return download.getDownloaded();
				case 4:
					return download.getSpeed();
			}
			return "ERROR";
		}
		
		@Override
		public String getColumnName(int col) 
		{
			return columnNames[col];
		}
		
		@Override
		public int getRowCount() 
		{
			int rv;
			synchronized (downloads) 
			{
				rv = downloads.size();
			}
			return rv;
		}
		
		@Override
		public int getColumnCount() 
		{
			return 5;
		}
	};
	
	public DownloadPanel()
	{
		super(new BorderLayout());
		downloads = new LinkedList<DownloadCmd>();
		JTable downloadTable = new JTable(dowloadTableModel);
		//downloadTable.getColumn("Progress").setCellRenderer(new ProgressCellRender());
		this.add(new JScrollPane(downloadTable), BorderLayout.CENTER);
		
	}

}
