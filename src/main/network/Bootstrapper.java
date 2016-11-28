package main.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;

import main.Main;
import main.factory.NodeFactory;
import main.record.NodeRecord;


public class Bootstrapper extends Thread 
{
	int bootstrapLimit = 8;
	
	public void run()
	{
		addBootstrapNodes();		
		performBootstrapping();
	}
	
	private void addBootstrapNodes()
	{
		// LAN nodes
		try {
			ArrayList<InetAddress> myIPs = Main.getMyIPs();
			for (InetAddress myIP : myIPs) {
				String[] ipParts = myIP.getHostAddress().split("\\.");
				int x = 1;
				while (x <=254)
	    		{
	    			String host = ipParts[0]+"."+ipParts[1]+"."+ipParts[2]+"."+Integer.toString(x);
	    			NodeFactory.createNew(host, 9991);
	    			x++;
	    		}
			}
		} catch (IOException e) {
			System.out.println("Error added LAN nodes.");
		}
		
		// Internet nodes
		ArrayList<String> internetNodes = new ArrayList<String>();
		internetNodes.add("81.108.218.180:9991");
		
		for (String internetNode : internetNodes) {
			String[] internetNodeParts = internetNode.split(":");
			NodeFactory.createNew(internetNodeParts[0], Integer.parseInt(internetNodeParts[1]));
		}
		
	}
	
	private void performBootstrapping()
	{
		ArrayList<NodeRecord> nodes = null;
		
		while(true)
		{
			nodes = NodeFactory.getRecentNodes();
			connectToNodes(nodes);
			
			try 
			{
				Thread.sleep(4000); // 4 seconds
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			nodes = NodeFactory.getRandomNodes();
			connectToNodes(nodes);
			
			try 
			{
				Thread.sleep(4000); // 4 seconds
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	private void connectToNodes(ArrayList<NodeRecord> nodes)
	{
		Iterator<NodeRecord> nodeIterator = nodes.iterator();
		NodeRecord node = null;
		
		ArrayList<PeerConnection> connections = new ArrayList<PeerConnection>();
		
		while(nodeIterator.hasNext())
		{
			node = nodeIterator.next();
			
			if (node==null) continue;
			
			// Prevent bootstrapping a connection to our external IP address
			if (node.host.equals(Main.peerServer1.externalIPAddress)) {
				continue;
			}
			
			connections.clear();
			connections.addAll(Main.peerServer1.connections);
			
			if (connections.size()>=bootstrapLimit) continue;
			if (isNodeAlreadyConnected(node, connections)) continue;
			
			try
        	{
        		SocketAddress sockAddr = new InetSocketAddress(node.host, node.port);
        		Socket newSocket = new Socket();
        		newSocket.connect(sockAddr, 2500);
				PeerConnection newPeerConnection = new PeerConnection(newSocket);
				Main.peerServer1.connections.add(newPeerConnection);
				newPeerConnection.start();
				//System.out.println("Bootstrap to "+node.host+":"+node.port+" succeeded.");
			}
        	catch (IOException e1) 
			{
        		//System.out.println("Bootstrap to "+node.host+":"+node.port+" failed.");
			}
		}
	}
	
	private boolean isNodeAlreadyConnected(NodeRecord node, ArrayList<PeerConnection> connections)
	{
		Iterator<PeerConnection> connectionIterator = connections.iterator();
		InetAddress connectionInetAddress = null;
		
		while(connectionIterator.hasNext())
		{
			connectionInetAddress = connectionIterator.next().socket.getInetAddress();
			if (connectionInetAddress.getHostAddress().equals(node.host) || connectionInetAddress.getHostName().equals(node.host))
			{
				return true;
			}
		}
		return false;
	}
}
