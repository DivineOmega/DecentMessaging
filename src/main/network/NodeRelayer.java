package main.network;

import java.util.ArrayList;
import java.util.Collections;

import main.Main;
import main.factory.NodeFactory;


public class NodeRelayer extends Thread 
{
	
	public void run()
	{		
		ArrayList<PeerConnection> connections = null;
		
		while(true)
		{
		
			try 
			{
				Thread.sleep(60*1000);
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
				connections.get(0).OutgoingObjects.add(NodeFactory.getRandomNode());
			}
			
		}
	}
}
