package main;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.UIManager;

import main.factory.PrivateKeyFactory;
import main.factory.PublicKeyFactory;
import main.gui.GUIUpdater;
import main.gui.MainWindow;
import main.network.Bootstrapper;
import main.network.LocalServer;
import main.network.MessageRelayer;
import main.network.NodeRelayer;
import main.network.PeerServer;

import org.apache.commons.codec.binary.Base64;

public class Main 
{
	public static LocalServer localServer1;
	public static PeerServer peerServer1;
	public static String dmAddress = null;
	public static String storageDirectory = null;
	
	public static MainWindow mainWindow = null; 
	
	public static int peerServerPort = 9991;
	public static int localServerPort = 8881;
	
	public static void main(String[] args)
	{
		System.setProperty("line.separator", "\n");
				
		System.out.println("*** Decent Messaging ***");
		
		System.out.println("Interpreting command line parameters...");
		
		storageDirectory = System.getProperty("user.home") + System.getProperty("file.separator") + 
				".decentmessaging" + System.getProperty("file.separator");
		
		boolean showGUI = true;
		boolean commandLineInterfaceMode = false;
		
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
			} else if (arg.equalsIgnoreCase("--command")) {
				System.out.println("Command line interface mode.");
				showGUI = false;
				commandLineInterfaceMode = true;
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
				
		if (!showGUI) {
			
			System.out.println("Skipping display of GUI, as requested...");
			
		} else {
        	
			System.out.println("Displaying main window...");
        	
        	mainWindow = new MainWindow();
    		mainWindow.show();
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
		
		checkAndCreateKeyPair();
		
		if (commandLineInterfaceMode) {
			CLIHandler.start(args);
			return;
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
		
		GUIUpdater guiUpdater = null;
		if (showGUI) {
			System.out.println("Starting GUI updater...");
			guiUpdater = new GUIUpdater();
			guiUpdater.start();
		}

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
		if (guiUpdater!=null) {
			threadsToMonitor.add(guiUpdater);
		}
				
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
	
	private static void checkAndCreateKeyPair() {
		
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
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
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
