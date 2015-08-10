package main.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import main.Main;
import main.network.handlers.IncomingHandler;
import main.network.handlers.OutgoingHandler;
import main.record.MessageRecord;
import main.record.NodeRecord;

public class PeerConnection extends Thread 
{
	Socket socket;
	DataInputStream in;
	DataOutputStream out;
	boolean threadDone = false;
	ArrayList<Object> OutgoingObjects = new ArrayList<Object>();
	
	public PeerConnection(Socket socket)
	{
		this.socket = socket;
	}
	
	public void done() 
	{
		threadDone = true;
	}
	
	public void run()
	{
		if(socket.getLocalAddress().equals(socket.getInetAddress()))
		{
			NodeRecord.deleteByHostAndPort(socket.getInetAddress().getHostAddress(), socket.getPort());
			NodeRecord.deleteByHostAndPort(socket.getInetAddress().getHostName(), socket.getPort());
			threadDone = true;
		}
		
		if (!threadDone)
		{
			try 
			{
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				socket.setSoTimeout(2500);
			}
			catch (IOException e) 
			{
				threadDone = true;
				e.printStackTrace();
			}
		}
		
		while (!threadDone)
		{
			handleIncoming(); 
			if (threadDone) break; 
			handleOutgoing(); 
			if (threadDone) break;
		}
		
		try 
		{
			socket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		if (!Main.peerServer1.connections.remove(this))
		{
			System.out.println("Cleanup issue: Could not remove PeerConnection object from PeerServer.connections list.");
			System.exit(0);
		}
	}
	
	public void handleIncoming()
	{
		try 
		{
			char command = in.readChar();
			if (command=='m') // Incoming message
			{
				if (IncomingHandler.receiveMessage(in, out))
				{
					NodeRecord.updateLastSeenBySocket(socket);
				}
			}
		} 
		catch (SocketTimeoutException e)
		{
			return;
		}
		catch (IOException e) 
		{
			threadDone = true;
			e.printStackTrace();
		}
	}
	
	private void handleOutgoing() 
	{
		try 
		{
			if (OutgoingObjects.size()==0) return;
			Object OutgoingObject = OutgoingObjects.get(0);
			OutgoingObjects.remove(0);
			if (OutgoingObject==null) return;
			if (OutgoingObject.getClass() == MessageRecord.class)
			{
				if (OutgoingHandler.sendMessage(in, out, (MessageRecord) OutgoingObject))
				{
					NodeRecord.updateLastSeenBySocket(socket);
				}
			}
		} 
		catch (SocketTimeoutException e)
		{
			return;
		}
		catch (IOException e) 
		{
			threadDone = true;
			e.printStackTrace();
		}
	}
	
}