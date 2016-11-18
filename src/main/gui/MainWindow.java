package main.gui;

import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultListModel;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.border.BevelBorder;

public class MainWindow {

	private JFrame frame;
	private JLabel lblActiveConnections;
	private ListModel listModel;

	public MainWindow() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Decent Messaging");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, "cell 0 0,grow");
		
		JPanel connectionsPanel = new JPanel();
		tabbedPane.addTab("Connections", null, connectionsPanel, null);
		connectionsPanel.setLayout(new MigLayout("", "[grow]", "[][][grow]"));
		
		lblActiveConnections = new JLabel("Active connections: 0");
		connectionsPanel.add(lblActiveConnections, "cell 0 0");
		
		listModel = new DefaultListModel<>();
		JList connectionsList = new JList(listModel);
		connectionsList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		connectionsPanel.add(connectionsList, "cell 0 2,grow");
		
		JPanel aboutPanel = new JPanel();
		tabbedPane.addTab("About", null, aboutPanel, null);
		aboutPanel.setLayout(new MigLayout("", "[]", "[]"));
	}
	
	public void show() {
		frame.setVisible(true);
	}

	public void updateActiveConnectionsCount(int number) {
		lblActiveConnections.setText("Active connections: "+number)	;
	}
	
	public void clearConnectionsList() {
		((DefaultListModel<Object>) listModel).clear();
	}
	
	public void addToConnectionsList(String host, int port) {
		((DefaultListModel<Object>) listModel).addElement(host+":"+port);
	}

}
