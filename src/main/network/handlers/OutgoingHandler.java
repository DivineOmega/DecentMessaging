package main.network.handlers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import main.record.MessageRecord;
import main.record.NodeRecord;

public abstract class OutgoingHandler 
{
	private static int sendMessageVersionNumber = 1;
	private static int sendNodeVersionNumber = 1;
	
	public static boolean sendMessage(DataInputStream in, DataOutputStream out, MessageRecord message) throws SocketTimeoutException, IOException
	{
		out.writeChar('m');
		out.writeInt(sendMessageVersionNumber);
		out.writeUTF(message.content_sha256);
		boolean needsMessage = in.readBoolean(); 
		if (needsMessage)
		{
			out.writeInt(message.iv.length);
			out.writeInt(message.content_key.length);
			out.writeInt(message.signature.length);
			out.writeInt(message.content_size);
			boolean willRelay = in.readBoolean();
			if (willRelay)
			{
				out.write(message.iv);
				out.write(message.content_key);
				out.write(message.signature);
				out.write(message.getContent());
				boolean messageAccepted = in.readBoolean();
				if (messageAccepted)
				{
					message.alterTimesBroadcast(1);
				}
			}
		}
		else
		{
			message.alterTimesBroadcast(1);
		}
		return true;
	}

	public static boolean sendNode(DataInputStream in, DataOutputStream out, NodeRecord outgoingObject) throws SocketTimeoutException, IOException 
	{
		out.writeChar('n');
		out.writeInt(sendNodeVersionNumber);
		out.writeUTF(outgoingObject.host);
		out.writeInt(outgoingObject.port);
		
		return true;
	}
}
