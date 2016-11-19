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
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (Main.mainWindow == null || Main.peerServer1 == null || Main.peerServer1.connections == null) {
				continue;
			}
			
			connections.clear();
			connections.addAll(Main.peerServer1.connections);
			
			Main.mainWindow.updateActiveConnectionsCount(connections.size());
			
			Main.mainWindow.updateConnectionsList(connections);
							
			Main.mainWindow.updateMyDecentMessagingAddress(Main.dmAddress);
			
		}
	}

}
