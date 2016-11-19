package main.gui;

import javax.swing.JFrame;

import main.Main;
import main.factory.NodeFactory;
import main.network.PeerConnection;
import main.record.NodeRecord;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.border.BevelBorder;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JTextPane;

public class MainWindow {

	private JFrame frame;
	private JLabel lblActiveConnections;
	private DefaultListModel<String> listModel;
	private JTextField textNodeToAdd;
	private JTextArea textAreaDecentMessagingAddress;

	public MainWindow() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Decent Messaging");
		frame.setBounds(100, 100, 450, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[grow]", "[grow]"));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, "cell 0 0,grow");
		
		JPanel connectionsPanel = new JPanel();
		tabbedPane.addTab("Connections", null, connectionsPanel, null);
		connectionsPanel.setLayout(new MigLayout("", "[grow]", "[][][grow][]"));
		
		lblActiveConnections = new JLabel("Active connections: 0");
		connectionsPanel.add(lblActiveConnections, "cell 0 0");
		
		listModel = new DefaultListModel<>();
		JList connectionsList = new JList(listModel);
		connectionsPanel.add(connectionsList, "flowy,cell 0 2,grow");
		
		JLabel lblManuallyAddNode = new JLabel("Manually add node:");
		connectionsPanel.add(lblManuallyAddNode, "flowx,cell 0 3");
		
		textNodeToAdd = new JTextField();
		textNodeToAdd.setToolTipText("Example: 192.168.0.5:9991");
		connectionsPanel.add(textNodeToAdd, "cell 0 3,growx");
		textNodeToAdd.setColumns(10);
				
		JButton btnAdd = new JButton("Add");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				String input = textNodeToAdd.getText();
				String[] inputParts = input.split(":");
				
				String host = inputParts[0].trim();
				String port = Integer.toString(Main.peerServerPort);;
				
				if (inputParts.length>=2) {
					port = inputParts[1].trim(); 
				}
	        	
	        	if (host.equals(""))
	        	{
	        		JOptionPane.showMessageDialog(null, "Please enter the node to add in the format IP_ADDRESS_OR_HOST:PORT.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
	        		return;
	        	}
	        	
	        	try
	        	{
	        		NodeRecord node = NodeFactory.createNew(host, Integer.valueOf(port));
		        	if (node!=null)
	        		{
		        		node.updateLastSeen();
	        			JOptionPane.showMessageDialog(null, "Node added: "+host+":"+port, "Decent Messaging", JOptionPane.INFORMATION_MESSAGE);
	        			textNodeToAdd.setText("");
	        		}
	        		else
	        		{
	        			JOptionPane.showMessageDialog(null, "Error adding node: "+host+":"+port, "Decent Messaging", JOptionPane.ERROR_MESSAGE);
	        		}
	        	}
	        	catch(NumberFormatException e1)
	        	{
	        		JOptionPane.showMessageDialog(null, "Port number must be numeric.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
	        	}
			}
		});
		connectionsPanel.add(btnAdd, "cell 0 3");
		
		JPanel messagingPanel = new JPanel();
		tabbedPane.addTab("Messaging", null, messagingPanel, null);
		messagingPanel.setLayout(new MigLayout("", "[grow]", "[][][grow][]"));
		
		JLabel lblYourDecentMessaging = new JLabel("Your Decent Messaging Address:");
		messagingPanel.add(lblYourDecentMessaging, "cell 0 0");
		
		textAreaDecentMessagingAddress = new JTextArea();
		textAreaDecentMessagingAddress.setEditable(false);
		textAreaDecentMessagingAddress.setLineWrap(true);
		messagingPanel.add(textAreaDecentMessagingAddress, "cell 0 2,grow");
		
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
		
	public void updateConnectionsList(ArrayList<PeerConnection> connections) {
		
		for (PeerConnection connection : connections) {
			
			boolean needToAdd = true;
			String connectionString = connection.getHostAddress();
			
			for (int j = 0; j < listModel.size(); j++) {
				if (listModel.get(j).equals(connectionString)) {
					needToAdd = false;
				}
			}
			
			if (needToAdd) {
				listModel.addElement(connectionString);
			}
		}
		
		for (int j = 0; j < listModel.size(); j++) {
		
			boolean needToRemove = true;
			
			for (PeerConnection connection : connections) {
				String connectionString = connection.getHostAddress();
				if (listModel.get(j).equals(connectionString)) {
					needToRemove = false;
				}
			}
			
			if (needToRemove) {
				listModel.removeElementAt(j);
			}
		}
	
		

	}
	
	public void updateMyDecentMessagingAddress(String decentMessagingAddress) {
		if (!textAreaDecentMessagingAddress.getText().equals(decentMessagingAddress)) {
			textAreaDecentMessagingAddress.setText(decentMessagingAddress);
		}
	}

	

}
