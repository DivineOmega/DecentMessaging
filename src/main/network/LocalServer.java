package main.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Iterator;

import main.Main;
import main.factory.MessageFactory;
import main.factory.PersonalFactory;
import main.record.MessageRecord;
import main.record.PersonalRecord;

import org.apache.commons.codec.binary.Base64;


public class LocalServer extends Thread 
{
	int port;
	
	public LocalServer(int port)
	{
		this.port = port;
	}
	
	public void run()
	{
		ServerSocket serverSocket = null;
		try 
		{
			serverSocket = new ServerSocket(port, 0, InetAddress.getByName("127.0.0.1"));
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.out.println("Error starting local server.");
			System.exit(0);
		}
		
		while (true)
		{
			Socket socket = null;
			try 
			{
				socket = serverSocket.accept();
				socket.setSoTimeout(5000);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out.println("*100 Command:");
				String cmd = in.readLine();
				if (cmd.equals("send")) // Send a message
				{
					// Read recipient modulus from 'in'
					out.println("*101 Recipient address:");
					String rcptAddress = in.readLine();
							
					BigInteger rcptModulus = Main.getModulusFromDmAddress(rcptAddress);
					BigInteger rcptExponent = Main.getExponentFromDmAddress(rcptAddress);
					
					if (rcptModulus == null || rcptExponent == null) {
						out.println("*205 Error: recipient DM address version string is not valid/supported.");
						socket.close();
						continue;
					}
					
					// Calculate recipient public key
					RSAPublicKeySpec keySpec = new RSAPublicKeySpec(rcptModulus, rcptExponent);
					KeyFactory fact;
					PublicKey rcptPublicKey;
					try 
					{
						fact = KeyFactory.getInstance("RSA");
						rcptPublicKey = fact.generatePublic(keySpec);
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
						out.println("*201 Error: internal error calculating recipient public key.");
						socket.close();
						continue;
					}
					
					// Read subject from 'in'
					out.println("*103 Subject:");
					String subject = in.readLine();
					
					// Read content from 'in'
					out.println("*104 Content:");
					String content = "";
					String toAdd = "";
					while (true)
					{
						toAdd = in.readLine();
						if (toAdd.equals(".")) break;
						content += toAdd + "\n";
					}
					if (content.equals(""))
					{
						out.println("*202 Error: you need to supply data.");
						socket.close();
						continue;
					}
					
					// Prefix content with subject
					content = subject + "\n" + content;
					
					// Prefix content with DM Address
					content = Main.dmAddress + "\n" + content;
					
					// Create new message
					MessageRecord newMsg = null;
					try 
					{
						newMsg = MessageFactory.encryptContentAndCreateNew(content.getBytes("UTF-8"), rcptPublicKey);
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
						out.println("*203 Error: internal error creating new message.");
						socket.close();
						continue;
					} 
					if (newMsg==null)
					{
						out.println("*204 Error: internal error verifying message creation.");
						socket.close();
						continue;
					}
					
					out.println("*300 Success: Message queued for delivery."); // Success
					socket.close();
					continue;
				}
				else if(cmd.equals("get"))
				{
					out.println("*111 Personal message ID:");
					String personalIDString = in.readLine();
					int personalID = 0;
					try 
					{
						personalID = Integer.valueOf(personalIDString);
					}
					catch(NumberFormatException e)
					{
						out.println("*211 Error: non-numeric personal ID.");
						socket.close();
						continue;
					}
					PersonalRecord personalRecord = PersonalFactory.get(personalID);
					if (personalRecord==null)
					{
						out.println("*212 Error: personal message with that ID does not exist.");
						socket.close();
						continue;
					}
					out.println("*310 Message begins.");
					out.println(personalRecord.originally_received.getTime());
					out.println(Main.createDmAddress(personalRecord.publickey_modulus, personalRecord.publickey_exponent));
					out.println(personalRecord.subject);
					out.println(personalRecord.getContentAsString());
					out.println("*311 Message ends."); // Success
					socket.close();
					continue;
				}
				else if(cmd.equals("delete"))
				{
					out.println("*141 Personal message ID:");
					String personalIDString = in.readLine();
					int personalID = 0;
					try 
					{
						personalID = Integer.valueOf(personalIDString);
					}
					catch(NumberFormatException e)
					{
						out.println("*241 Error: non-numeric personal ID.");
						socket.close();
						continue;
					}
					PersonalRecord personalRecord = PersonalFactory.get(personalID);
					if (personalRecord==null)
					{
						out.println("*242 Error: personal message with that ID does not exist.");
						socket.close();
						continue;
					}
					boolean deleted = personalRecord.delete();
					if (!deleted) {
						out.println("*243 Error: personal message may not have been deleted successfully.");
					} else {
						out.println("*340 Success: personal message deleted.");
					}
					socket.close();
					continue;
				}
				else if(cmd.equals("list"))
				{
					out.println("*121 Timestamp:");
					String timestampString = in.readLine();
					long timestamp = 0;
					try
					{
						timestamp = Long.valueOf(timestampString);
					}
					catch(NumberFormatException e)
					{
						out.println("*221 Error: non-numeric timestamp.");
						socket.close();
						continue;
					}
					ArrayList<Integer> ids = PersonalFactory.getIDsAfterTimestamp(new java.sql.Timestamp(timestamp));
					Iterator<Integer> idIterator = ids.iterator();
					out.println("*320 List begins."); // Success
					while (idIterator.hasNext())
					{
						out.print(idIterator.next());
						out.println();
					}
					out.println("*321 List ends."); // Success
					socket.close();
					continue;
				}
				else if(cmd.equals("me"))
				{
					out.println("*330 DM address of this node:");
					out.println(Main.dmAddress);
					socket.close();
					continue;
				}
				else
				{
					out.println("*200 Error: invalid command.");
					socket.close();
					continue;
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
				try 
				{
					socket.close();
				} 
				catch (IOException e1) 
				{
					e1.printStackTrace();
				}
				
				System.out.println("Error with local server connection.");
			}
			
		}
	}
}
