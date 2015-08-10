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
		ArrayList<NodeRecord> nodes = null;
		
		while(true)
		{
			nodes = NodeFactory.getRecentNodes();
			connectToNodes(nodes);
			
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			nodes = NodeFactory.getRandomNodes();
			connectToNodes(nodes);
			
			try 
			{
				Thread.sleep(1000);
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
