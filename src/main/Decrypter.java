package main;

import main.factory.MessageFactory;
import main.factory.PersonalFactory;
import main.factory.PrivateKeyFactory;
import main.record.MessageRecord;



public class Decrypter extends Thread 
{
	int port;
	
	public void run()
	{
		while(true)
		{
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			MessageRecord messageToDecrypt = MessageFactory.getMessageToDecrypt();
			if (messageToDecrypt==null) continue;
			try 
			{
				byte[] decryptedContent = messageToDecrypt.getDecryptedContent(PrivateKeyFactory.get(1).getKey());
				if (messageToDecrypt.verifySignature(decryptedContent))
				{
					PersonalFactory.createNew(decryptedContent);
				}
				//System.out.println("Decrypting message "+messageToDecrypt.id+" - OKAY");
			} 
			catch (Exception e) 
			{
				//System.out.println("Decrypting message "+messageToDecrypt.id+" - FAIL - "+e.getMessage());
				//e.printStackTrace();
			} 
			messageToDecrypt.setDecryptionAttempted(true);
		}
	}
}
