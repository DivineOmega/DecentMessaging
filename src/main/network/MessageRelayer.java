package main.network;

import java.util.ArrayList;
import java.util.Iterator;

import main.Main;
import main.factory.MessageFactory;


public class MessageRelayer extends Thread 
{
	
	public void run()
	{		
		ArrayList<PeerConnection> connections = null;
		Iterator<PeerConnection> connectionIterator = null;
		
		while(true)
		{
		
			try 
			{
				Thread.sleep(500); // 0.5 seconds
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			connections = new ArrayList<PeerConnection>();
			connections.addAll(Main.peerServer1.connections);
			connectionIterator = connections.iterator();
			
			while(connectionIterator.hasNext())
			{
				connectionIterator.next().OutgoingObjects.add(MessageFactory.getMessageToRelay());
			}
		}
	}
}
