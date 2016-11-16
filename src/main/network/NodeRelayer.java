package main.network;

import java.util.ArrayList;
import java.util.Collections;

import main.Main;
import main.factory.NodeFactory;
import main.record.NodeRecord;


public class NodeRelayer extends Thread 
{
	
	public void run()
	{		
		ArrayList<PeerConnection> connections = null;
		
		while(true)
		{
		
			try 
			{
				Thread.sleep(10*1000); // 10 seconds
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			connections = new ArrayList<PeerConnection>();
			connections.addAll(Main.peerServer1.connections);
			Collections.shuffle(connections);
			
			if (connections.size()>0)
			{
				NodeRecord nodeToRelay = NodeFactory.getNodeToRelay();
				
				if (nodeToRelay != null) 
				{
					connections.get(0).OutgoingObjects.add(nodeToRelay);
				}
			}
			
		}
	}
}
