package main;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import main.factory.NodeFactory;
import main.factory.PrivateKeyFactory;
import main.factory.PublicKeyFactory;
import main.network.Bootstrapper;
import main.network.LocalServer;
import main.network.MessageRelayer;
import main.network.NodeRelayer;
import main.network.PeerServer;
import main.record.NodeRecord;

import org.apache.commons.codec.binary.Base64;

public class Main 
{
	static LocalServer localServer1;
	public static PeerServer peerServer1;
	public static String dmAddress = null;
	public static String storageDirectory = null;
	
	public static void main(String[] args)
	{
		System.setProperty("line.separator", "\n");
		
		System.out.println("*** Decent Messaging ***");
		
		System.out.println("Interpreting command line parameters...");
		
		storageDirectory = System.getProperty("user.home") + System.getProperty("file.separator") + 
				".decentmessaging" + System.getProperty("file.separator");
		
		int peerServerPort = 9991;
		int localServerPort = 8881;
		boolean showGUI = true;
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			
			if (arg.equalsIgnoreCase("--peer-server-port")) {
				if (args.length >= i) {
					try {
						peerServerPort = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException e) {
						System.out.println("Invalid peer server port number.");
						System.exit(1);
					}
				}
			} else if (arg.equalsIgnoreCase("--local-server-port")) {
				if (args.length >= i) {
					try {
						localServerPort = Integer.parseInt(args[i+1]);
					} catch (NumberFormatException e) {
						System.out.println("Invalid local server port number.");
						System.exit(1);
					}
				}
			} else if (arg.equalsIgnoreCase("--hidden")) {
				showGUI = false;
			} else if (arg.equalsIgnoreCase("--portable")) {
				storageDirectory = "." + System.getProperty("file.separator") + 
						".decentmessaging" + System.getProperty("file.separator");
			}
		}
		
		System.out.println("Setting system look and feel...");
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch (Exception e) 
		{
			System.out.println("Error setting look and feel.");
		}
		
		
		MenuItem removeNodeMenuItem = null;
		PopupMenu nodePopup = null;
		MenuItem dmAddressMenuItem = null;
		
		if (!showGUI) {
			
			System.out.println("Skipping display of GUI, as requested...");
			
		} else if (!SystemTray.isSupported()) {
			
            System.out.println("SystemTray is not supported.");
            
        } else {
        	
			System.out.println("Adding system tray icon...");
			
			SystemTray tray = SystemTray.getSystemTray();
			URL imageURL = Main.class.getResource("resources/images/systemtrayicon.gif");
			Image image = Toolkit.getDefaultToolkit().getImage(imageURL);
			PopupMenu popup = new PopupMenu();
			dmAddressMenuItem = new MenuItem("Copy your DM Address");
		    ActionListener dmAddressMenuItemListener = new ActionListener() 
			{
		        public void actionPerformed(ActionEvent e) 
		        {
		        	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		            clipboard.setContents(new StringSelection(dmAddress), null);
		        	JOptionPane.showMessageDialog(null, "Your Decent Messaging address has been copied to the clipboard.", "Decent Messaging", JOptionPane.INFORMATION_MESSAGE);
		        }
		    };
		    dmAddressMenuItem.addActionListener(dmAddressMenuItemListener);
		    dmAddressMenuItem.setEnabled(false);
		    popup.add(dmAddressMenuItem);
		    nodePopup = new PopupMenu("Connections");
		    nodePopup.setEnabled(false);
		    MenuItem addNodeMenuItem = new MenuItem("Add node...");
		    ActionListener addNodeMenuItemListener = new ActionListener() 
			{
		        public void actionPerformed(ActionEvent e) 
		        {
		        	String host = JOptionPane.showInputDialog(null, "Hostname or IP address:", "Decent Messaging", JOptionPane.QUESTION_MESSAGE);
		        	if (host==null) return;
		        	host = host.trim();
		        	if (host.equals(""))
		        	{
		        		JOptionPane.showMessageDialog(null, "No hostname or IP address entered.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
		        		return;
		        	}
		        	String port = (String) JOptionPane.showInputDialog(null, "Port number:", "Decent Messaging", JOptionPane.QUESTION_MESSAGE, null, null, 9991);
		        	if (port==null) return;
		        	port = port.trim();
		        	if (port.equals(""))
		        	{
		        		JOptionPane.showMessageDialog(null, "No port number entered.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
		        		return;
		        	}
		        	try
		        	{
		        		NodeRecord node = NodeFactory.createNew(host, Integer.valueOf(port));
			        	if (node!=null)
		        		{
			        		node.updateLastSeen();
		        			JOptionPane.showMessageDialog(null, "Node added: "+host+":"+port, "Decent Messaging", JOptionPane.INFORMATION_MESSAGE);
		        		}
		        		else
		        		{
		        			JOptionPane.showMessageDialog(null, "Error adding node: "+host+":"+port, "Decent Messaging", JOptionPane.ERROR_MESSAGE);
		        		}
		        	}
		        	catch(NumberFormatException e1)
		        	{
		        		JOptionPane.showMessageDialog(null, "Port number was not numeric.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
		        	}
		        }
		    };
		    addNodeMenuItem.addActionListener(addNodeMenuItemListener);
		    nodePopup.add(addNodeMenuItem);
		    /*MenuItem addLANNodesMenuItem = new MenuItem();
		    try
			{
				ArrayList<InetAddress> myIPs = getMyIPs();
				if (myIPs.size()>0)
				{
					String[] ipParts = myIPs.get(0).getHostAddress().split("\\.");
					addLANNodesMenuItem.setLabel("Add LAN nodes ("+ipParts[0]+"."+ipParts[1]+"."+ipParts[2]+".1-254"+")");
					ActionListener addLANNodesMenuItemListener = new ActionListener() 
					{
				        public void actionPerformed(ActionEvent e) 
				        {
							try
							{
								ArrayList<InetAddress> myIPs = getMyIPs();
								String[] ipParts = myIPs.get(0).getHostAddress().split("\\.");
								int option = JOptionPane.showConfirmDialog(null, "Are you sure add all LAN nodes in the range "+ipParts[0]+"."+ipParts[1]+"."+ipParts[2]+".1-254"+"?", "Decent Messaging", JOptionPane.YES_NO_OPTION);
					        	if (option == JOptionPane.NO_OPTION ) 
					        	{
						            return;
					        	}
								String port = (String) JOptionPane.showInputDialog(null, "Port number:", "Decent Messaging", JOptionPane.QUESTION_MESSAGE, null, null, 9991);
					        	if (port==null) return;
					        	port = port.trim();
					        	if (port.equals(""))
					        	{
					        		JOptionPane.showMessageDialog(null, "No port number entered.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
					        		return;
					        	}
				        		String host;
				        		int x = 1;
				        		while (x<=254)
				        		{
				        			host = ipParts[0]+"."+ipParts[1]+"."+ipParts[2]+"."+Integer.toString(x);
				        			NodeRecord node = NodeFactory.createNew(host, Integer.valueOf(port)); 
				        			if (node==null)
					        		{
					        			JOptionPane.showMessageDialog(null, "Error adding node: "+host+":"+port+". No further LAN nodes will be added.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
					        			return;
					        		}
				        			else
				        			{
				        				node.updateLastSeen();
				        			}
				        			x++;
				        		}
				        		JOptionPane.showMessageDialog(null, "All LAN nodes in the range "+ipParts[0]+"."+ipParts[1]+"."+ipParts[2]+".1-254 have been added.", "Decent Messaging", JOptionPane.INFORMATION_MESSAGE);
							} 
							catch (SocketException e1)
							{
								e1.printStackTrace();
								JOptionPane.showMessageDialog(null, "Error determining LAN IP address.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
							}
				        }
				    };
				    addLANNodesMenuItem.addActionListener(addLANNodesMenuItemListener);
				}
			} 
		    catch (SocketException e2)
			{
				e2.printStackTrace();
			}
		    nodePopup.add(addLANNodesMenuItem);*/
		    removeNodeMenuItem  = new MenuItem("Remove node...");
		    ActionListener removeNodeMenuItemListener = new ActionListener()
		    {
		    	public void actionPerformed(ActionEvent e) 
		        {
		    		String host = JOptionPane.showInputDialog(null, "Hostname or IP address:", "Decent Messaging", JOptionPane.QUESTION_MESSAGE);
		        	if (host==null) return;
		        	host = host.trim();
		        	if (host.equals(""))
		        	{
		        		JOptionPane.showMessageDialog(null, "No hostname or IP address entered.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
		        		return;
		        	}
		        	String port = (String) JOptionPane.showInputDialog(null, "Port number:", "Decent Messaging", JOptionPane.QUESTION_MESSAGE, null, null, 9991);
		        	if (port==null) return;
		        	port = port.trim();
		        	if (port.equals(""))
		        	{
		        		JOptionPane.showMessageDialog(null, "No port number entered.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
		        		return;
		        	}
		        	try
		        	{
			        	if (NodeRecord.deleteByHostAndPort(host, Integer.valueOf(port)))
		        		{
		        			JOptionPane.showMessageDialog(null, "Node deleted: "+host+":"+port, "Decent Messaging", JOptionPane.INFORMATION_MESSAGE);
		        		}
		        		else
		        		{
		        			JOptionPane.showMessageDialog(null, "Error deleting node: "+host+":"+port, "Decent Messaging", JOptionPane.ERROR_MESSAGE);
		        		}
		        	}
		        	catch(NumberFormatException e1)
		        	{
		        		JOptionPane.showMessageDialog(null, "Port number was not numeric.", "Decent Messaging", JOptionPane.ERROR_MESSAGE);
		        	}
		        }
			};
			removeNodeMenuItem.addActionListener(removeNodeMenuItemListener);
		    nodePopup.add(removeNodeMenuItem);
		    nodePopup.addSeparator();
		    MenuItem viewStatusMenuItem = new MenuItem("View connection status");
		    ActionListener viewStatusMenuItemListener = new ActionListener()
		    {
		    	public void actionPerformed(ActionEvent e) 
		        {
		    		int conTotal = Main.peerServer1.connections.size();
		    		String conStatusMsg = "";
		    		if (conTotal>=8) conStatusMsg = "You have a large number of connections. \nMany thanks for helping the network.";
		    		else if (conTotal>=2) conStatusMsg = "You have sufficient connections to actively relay messages for other users. \nThank you for helping the network. Adding additional nodes will further help the network.";
		    		else if (conTotal==1) conStatusMsg = "You can send and receive messages, but can not relay messages for other users. \nWe recommend you add additional nodes to help the network.";
		    		else if (conTotal==0) conStatusMsg = "You have no connections. Decent Messaging will not function until you have at least one or two connections. \nWe recommend you add additional nodes.";
		    		JOptionPane.showMessageDialog(null, "Connections: "+conTotal+"\n\n"+conStatusMsg, "Decent Messaging", JOptionPane.INFORMATION_MESSAGE);
		        }
			};
		    viewStatusMenuItem.addActionListener(viewStatusMenuItemListener);
		    nodePopup.add(viewStatusMenuItem);
		    popup.add(nodePopup);
		    MenuItem exitMenuItem = new MenuItem("Exit");
		    ActionListener exitMenuItemListener = new ActionListener() 
			{
		        public void actionPerformed(ActionEvent e) 
		        {
		        	int option = JOptionPane.showConfirmDialog(null, "Are you sure you wish to exit?\n\nNote: You will not be able to receive any new messages if you exit.", "Decent Messaging", JOptionPane.YES_NO_OPTION);
		        	if (option == JOptionPane.YES_OPTION ) 
		        	{
			            System.out.println("Exiting at user request, via system tray icon.");
			            System.exit(0);
		        	}
		        }
		    };
		    exitMenuItem.addActionListener(exitMenuItemListener);
		    popup.add(exitMenuItem);
			TrayIcon trayIcon = new TrayIcon(image, "Decent Messaging", popup);
			trayIcon.setImageAutoSize(true);
			try 
			{
				tray.add(trayIcon);
			} 
			catch (AWTException e1) 
			{
				System.out.println("Error adding system tray icon.");
			}
		}
	
		String directoryToCreate = Main.storageDirectory;
		System.out.println("Checking/creating main directory... "+directoryToCreate);
		if (!(new File(directoryToCreate)).exists() && !(new File(directoryToCreate)).mkdir())
		{
			System.out.println("Error creating directory at "+directoryToCreate);
			System.exit(0);
		}
		
		directoryToCreate  = Main.storageDirectory+"message";
		System.out.println("Checking/creating message directory... "+directoryToCreate);
		if (!(new File(directoryToCreate)).exists() &&!(new File(directoryToCreate)).mkdir())
		{
			System.out.println("Error creating directory at "+directoryToCreate);
			System.exit(0);
		}
		
		directoryToCreate  = Main.storageDirectory+"personal";
		System.out.println("Checking/creating personal directory... "+directoryToCreate);
		if (!(new File(directoryToCreate)).exists() &&!(new File(directoryToCreate)).mkdir())
		{
			System.out.println("Error creating directory at "+directoryToCreate);
			System.exit(0);
		}
		
		DatabaseConnection dbconn = new DatabaseConnection();
		System.out.println("Creating/opening database...");
		if (!dbconn.connect())
		{
			System.out.println("Error opening database.");
			System.exit(0);
		}
		System.out.println("Checking/setting up database schema...");
		if (!dbconn.setupSchema())
		{
			System.out.println("Error setting up database schema.");
			System.exit(0);
		}
		
		System.out.println("Checking main public/private keys...");
		if (PrivateKeyFactory.get(1)==null && PublicKeyFactory.get(1)==null)
		{
			System.out.println("Main public/private keys do not exist.");
			System.out.println("Creating new main public/private key pair...");
			try 
			{
				createMainKeyPair();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				System.out.println("Error creating new main public/private key pair.");
				System.exit(0);
			}
			System.out.println("Public/private key pair created.");
		}
		else if (PrivateKeyFactory.get(1)!=null && PublicKeyFactory.get(1)==null)
		{
			System.out.println("Consistency error. Main private key exists without corresponding public key.");
			System.exit(0);
		}
		else if (PrivateKeyFactory.get(1)==null && PublicKeyFactory.get(1)!=null)
		{
			System.out.println("Consistency error. Main public key exists without corresponding private key.");
			System.exit(0);
		}
		else
		{
			System.out.println("Main public/private key pair found.");
		}
		
		try 
		{
			dmAddress = createDmAddress(PublicKeyFactory.get(1).modulus, PublicKeyFactory.get(1).exponent);
			System.out.println("Your Decent Messaging address: "+dmAddress);
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
		
		System.out.println("Starting local server on port "+localServerPort+"...");
		localServer1 = new LocalServer(localServerPort);
		localServer1.start();
		
		System.out.println("Starting peer server on port "+peerServerPort+"...");
		peerServer1 = new PeerServer(peerServerPort);
		peerServer1.start();
		
		System.out.println("Starting message relayer...");
		MessageRelayer messageRelayer1 = new MessageRelayer();
		messageRelayer1.start();
		
		System.out.println("Starting node relayer...");
		NodeRelayer nodeRelayer1 = new NodeRelayer();
		nodeRelayer1.start();
		
		System.out.println("Starting bootstrapper...");
		Bootstrapper bootstrapper1 = new Bootstrapper();
		bootstrapper1.start();
		
		System.out.println("Starting message decrypter...");
		Decrypter decrypter1 = new Decrypter();
		decrypter1.start();
		
		System.out.println("Starting caretaker...");
		Caretaker caretaker1 = new Caretaker();
		caretaker1.start();
			
		System.out.println("Enabling system tray menu items...");
		if (dmAddressMenuItem!=null) dmAddressMenuItem.setEnabled(true);
		if (removeNodeMenuItem!=null) removeNodeMenuItem.setEnabled(true);
		if (removeNodeMenuItem!=null) nodePopup.setEnabled(true);
		
		System.out.println("Start up complete.");
		
		System.out.println("Thread monitoring starting...");
		
		ArrayList<Thread> threadsToMonitor = new ArrayList<Thread>();
		threadsToMonitor.add(localServer1);
		threadsToMonitor.add(peerServer1);
		threadsToMonitor.add(messageRelayer1);
		threadsToMonitor.add(nodeRelayer1);
		threadsToMonitor.add(bootstrapper1);
		threadsToMonitor.add(decrypter1);
		threadsToMonitor.add(caretaker1);
				
		while(true) {
			
			for (Thread thread : threadsToMonitor) {
				if (!thread.isAlive()) {
					System.out.println("Thread ID "+thread.getId()+" of type "+thread.getClass().getName()+" appears to have failed. Attempting to restart it...");
					
					try {
						
						Constructor<?>[] constructors = thread.getClass().getConstructors();
																	
						for (Constructor<?> constructor : constructors) {
							if (constructor.getParameterTypes().length==0) {
								thread = (Thread) constructor.newInstance();
							} else {
								if (constructor.getDeclaringClass().getName()=="main.network.PeerServer") {
									thread = (Thread) constructor.newInstance(peerServerPort);
								} else if (constructor.getDeclaringClass().getName()=="main.network.LocalServer") {
									thread = (Thread) constructor.newInstance(localServerPort);
								}
							}
						}
						
						thread.start();
						
						System.out.println("Succesfully restarted thread of type "+thread.getClass().getName()+" with Thread ID "+thread.getId()+".");
						
					
					} catch (InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
						System.out.println("Error restarting a monitored thread. This node is not fully functional and should be restarted.");
						e.printStackTrace();
						
					}
					
					
				}
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private static void createMainKeyPair() throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(4096);
		KeyPair kp = kpg.genKeyPair();
		PublicKeyFactory.createNew(1, kp);
		PrivateKeyFactory.createNew(1, kp);
	}
	
	public static String createDmAddress(BigInteger modulus, BigInteger exponent) throws UnsupportedEncodingException
	{
		String dmAddressVersion = "A";
		String base64Modulus = new String(Base64.encodeInteger(modulus), "UTF-8");
		String base64Exponent = new String(Base64.encodeInteger(exponent), "UTF-8");
		return dmAddressVersion+","+base64Modulus+","+base64Exponent;
	}
	
	public static BigInteger getModulusFromDmAddress(String dmAddress) throws UnsupportedEncodingException
	{
		String[] dmAddressParts = dmAddress.split(",");
		
		if (dmAddressParts.length<1) {
			return null;
		}
		
		String dmAddressVersion = dmAddressParts[0];
		
		if (dmAddressVersion.equals("A") && dmAddressParts.length==3) {
			return Base64.decodeInteger(dmAddressParts[1].getBytes("UTF-8"));
		}
		
		return null;
	}
	
	public static BigInteger getExponentFromDmAddress(String dmAddress) throws UnsupportedEncodingException
	{
String[] dmAddressParts = dmAddress.split(",");
		
		if (dmAddressParts.length<1) {
			return null;
		}
		
		String dmAddressVersion = dmAddressParts[0];
		
		if (dmAddressVersion.equals("A") && dmAddressParts.length==3) {
			return Base64.decodeInteger(dmAddressParts[2].getBytes("UTF-8"));
		}
		
		return null;
	}
	
	public static ArrayList<InetAddress> getMyIPs() throws SocketException
	{
		// Create new list to contain the IP addresses found from all connected LAN network interfaces
		ArrayList<InetAddress> myIps = new ArrayList<InetAddress>();
		
		// Enumerate all the connected network interfaces on the current system 
		Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();

		// Iterate through all network interfaces
		while (e.hasMoreElements())
		{
			// Assign current iteration network interface to temporary 'ni' variable
			NetworkInterface ni = (NetworkInterface) e.nextElement();
			
			// Check if the current iteration network interface is a loop back interface (local only), if so skip it.
			if (ni.isLoopback())
			{
				continue;
			}
			
			// Enumerate all IP addresses for the current iteration network interface	
			Enumeration<InetAddress> e2 = ni.getInetAddresses();
			
			// Iterate through all IP addresses for the current iteration network interface	
			while (e2.hasMoreElements())
			{
				// Assign current iteration IP address to temporary 'tmp' variable
				InetAddress tmp = e2.nextElement();
				
				// Check to ensure current iteration IP address is IPv4
				if (tmp.getClass().equals(Inet4Address.class))
				{
					// Assign 'tmp' to new 'ip' variable (not sure why this is required)
					InetAddress ip = (InetAddress) tmp;
					
					// Check to ensure IP address is a LAN IP address (rather than a WAN connection)
					// We do not want to do port scanning over an WAN interface 
					if (ip.isSiteLocalAddress())
					{
						// Add IP address to list of current LAN IP addresses
						myIps.add(ip);
					}
				}
			}
		}
		return myIps;
	}
}
