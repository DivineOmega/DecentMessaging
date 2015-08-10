package main.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import main.Main;


public class PeerServer extends Thread 
{
	int port;
	public ArrayList<PeerConnection> connections = new ArrayList<PeerConnection>();
	int incomingLimit = 40;
	
	public PeerServer(int port)
	{
		this.port = port;
	}
	
	public void run()
	{
		ServerSocket serverSocket = null;
		try 
		{
			serverSocket = new ServerSocket(port, 0);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.out.println("Error starting peer server.");
			System.exit(0);
		}
		
		Socket socket;
		while (true)
		{
			if (Main.peerServer1.connections.size()>=incomingLimit)
			{
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				continue;
			}
			
			try 
			{
				socket = serverSocket.accept();
				PeerConnection incomingConnection = new PeerConnection(socket);
				connections.add(incomingConnection);
				incomingConnection.start();
				
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				System.out.println("Error with peer server connection.");
			}
		}
	}
}
