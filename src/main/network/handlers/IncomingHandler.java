package main.network.handlers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.commons.codec.digest.DigestUtils;

import main.factory.MessageFactory;
import main.factory.NodeFactory;
import main.record.NodeRecord;

public abstract class IncomingHandler 
{
	public static boolean receiveMessage(DataInputStream in, DataOutputStream out) throws SocketTimeoutException, IOException
	{
		int sendMessageVersionNumber = in.readInt();
		
		if (sendMessageVersionNumber == 1)
		{
			String sha256 = in.readUTF();
			if (MessageFactory.getBySha256(sha256)==null) // Do I need this message?
			{
				out.writeBoolean(true); // Yes, I do not have this message - Message needed
				int ivLength = in.readInt(); // Read in IV length
				int keyLength = in.readInt(); // Read in key length
				int sigLength = in.readInt(); // Read in signature length
				int contentLength = in.readInt(); // Read in content length
				if (ivLength > 0 && ivLength <= 512 && sigLength > 0 && sigLength <= 512 && keyLength > 0 && keyLength <= 512 && contentLength>0 && contentLength<=10485760) // Will I relay messages of this length?
				{
					out.writeBoolean(true); // Yes, this is fine.
					
					// Read in iv
					byte[] iv = new byte[ivLength]; int offset = 0; int numRead = 0;
					while (offset < iv.length && (numRead=in.read(iv, offset, iv.length-offset)) >= 0) 
				    {
				        offset += numRead;
				    }
				    // Read in key
					byte[] encryptedKey = new byte[keyLength]; offset = 0; numRead = 0;
					while (offset < encryptedKey.length && (numRead=in.read(encryptedKey, offset, encryptedKey.length-offset)) >= 0) 
				    {
				        offset += numRead;
				    }
					// Read in signature
					byte[] sig = new byte[sigLength]; offset = 0; numRead = 0;
					while (offset < sig.length && (numRead=in.read(sig, offset, sig.length-offset)) >= 0) 
				    {
				        offset += numRead;
				    }
				    // Read in content
					byte[] encryptedContent = new byte[contentLength]; offset = 0; numRead = 0;
					while (offset < encryptedContent.length && (numRead=in.read(encryptedContent, offset, encryptedContent.length-offset)) >= 0) 
				    {
				        offset += numRead;
				    }
					
					// Create new message 
					try 
					{
						if (DigestUtils.sha256Hex(encryptedContent).equals(sha256))
						{
							if(MessageFactory.createNew(iv, encryptedKey, sig, encryptedContent)!=null)
							{
								out.writeBoolean(true);  // Succeeded
								return true;
							}
						}
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
			}
			out.writeBoolean(false); // No, I don't need this message.
			return false;
		}
		else
		{
			return false;
		}
	}

	public static boolean receiveNode(DataInputStream in, DataOutputStream out) throws IOException 
	{
		int sendNodeVersionNumber = in.readInt();
		
		if (sendNodeVersionNumber == 1)
		{
			
			String host = in.readUTF();
			int port = in.readInt();
			
			if (host.length() <=4 || host.length() >= 80) {
				return false;
			}
			
			if (port < 0 || port > 65535) {
				return false;
			}
			
			NodeRecord node = NodeFactory.createNew(host, port);
			
			if (node == null) {
				return false;
			} else {
				return true;
			}
			
		}
		else
		{
			return false;
		}
		
	}
}
