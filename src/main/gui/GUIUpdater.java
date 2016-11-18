package main.gui;

import java.util.ArrayList;

import main.Main;
import main.network.PeerConnection;


public class GUIUpdater extends Thread
{

	public void run()
	{	
		ArrayList<PeerConnection> connections = new ArrayList<PeerConnection>();
		
		while(true)
		{
			if (Main.peerServer1 != null && Main.peerServer1.connections != null) {
				
				connections.clear();
				connections.addAll(Main.peerServer1.connections);
				
				Main.mainWindow.updateActiveConnectionsCount(connections.size());
				
				Main.mainWindow.clearConnectionsList();
				
				for (PeerConnection peerConnection : connections) {
					Main.mainWindow.addToConnectionsList(peerConnection.getHostAddress(), peerConnection.getPortNumber());
				}
				
			}
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
