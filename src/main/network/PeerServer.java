package main.network;

import java.io.IOException;
import java.net.InetAddress;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import main.network.SSLContextFactory;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.xml.sax.SAXException;

import main.Main;
import main.factory.NodeFactory;


public class PeerServer extends Thread 
{
	int port;
	public ArrayList<PeerConnection> connections = new ArrayList<PeerConnection>();
	int incomingLimit = 40;
	String externalIPAddress;
	
	public PeerServer(int port)
	{
		this.port = port;
	}
	
	public void run()
	{
                SSLServerSocket serverSocket = null;
                try
                {
                        SSLServerSocketFactory factory = SSLContextFactory.newServerSocketFactory();
                        serverSocket = (SSLServerSocket) factory.createServerSocket(port, 0);
                }
                catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Error starting peer server.");
			System.exit(0);
		}
		
		try 
		{
			portMap();
		}
		catch (SAXException | IOException | ParserConfigurationException e) 
		{
			e.printStackTrace();
			System.out.println("Error mapping port on uPnP device to peer server.");
		}
		
                SSLSocket socket;
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
                                socket = (SSLSocket) serverSocket.accept();
                                socket.startHandshake();
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
	
	public void portMap() throws IOException, SAXException, ParserConfigurationException
	{
		GatewayDiscover discover = new GatewayDiscover();
		discover.discover();
		
		GatewayDevice d = discover.getValidGateway();
		
		if (d == null) {
			return;
		}
		
		
		
		InetAddress localAddress = d.getLocalAddress();
		externalIPAddress = d.getExternalIPAddress();
		
		boolean portMapped = false;
		
		// Remove port mapping (if present)
		d.deletePortMapping(this.port, "TCP");
		
		// Attempt to map port
		if (d.addPortMapping(this.port, this.port, localAddress.getHostAddress(),"TCP","Decent Messaging")) {
			portMapped = true;
		}
		
		// If we managed to map the port, we should add our external IP address and port to the nodes list.
		// This will allow it to be relayed to other nodes later.
		if (portMapped) {
			NodeFactory.createNew(externalIPAddress, this.port);
		}

	}
	
}
