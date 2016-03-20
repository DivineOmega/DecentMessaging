package main;

import java.io.File;

import main.factory.MessageFactory;
import main.record.MessageRecord;


public class Caretaker extends Thread 
{
	
	public void run()
	{
		File messageDirectory = new File(Main.storageDirectory+"message");
		long freeSpace = 0;
		long usedSpace = 0;
		
		while(true)
		{
			try 
			{
				Thread.sleep(10000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			freeSpace = messageDirectory.getFreeSpace();
			usedSpace = MessageRecord.getUsedSpace();
			
			while (usedSpace > (freeSpace*0.1))
			{
				MessageFactory.getMessageToDelete().deleteContentFile();
				usedSpace = MessageRecord.getUsedSpace();
			}
		}
	}
}
