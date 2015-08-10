package main.network.handlers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;

import main.record.MessageRecord;

public abstract class OutgoingHandler 
{
	public static boolean sendMessage(DataInputStream in, DataOutputStream out, MessageRecord message) throws SocketTimeoutException, IOException
	{
		out.writeChar('m');
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
					return true;
				}
			}
		}
		else
		{
			message.alterTimesBroadcast(1);
		}
		return false;
	}
}
