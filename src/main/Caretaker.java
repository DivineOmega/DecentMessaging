package main;

import java.io.File;

import main.factory.MessageFactory;
import main.record.MessageRecord;


public class Caretaker extends Thread 
{
	
	public void run()
	{
		File messageDirectory = new File(Main.storageDirectory+"message");
		double usedSpace = 0;
		double allocatedSpace = 0;
		
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
			
			allocatedSpace = messageDirectory.getTotalSpace() * 0.01;
			
			while (allocatedSpace > messageDirectory.getUsableSpace()) {
				allocatedSpace = allocatedSpace * 0.5;
			}
			
			usedSpace = MessageRecord.getUsedSpace();
			
			while (usedSpace > allocatedSpace)
			{
				MessageFactory.getMessageToDelete().deleteContentFile();
				usedSpace = MessageRecord.getUsedSpace();
			}
		}
	}
}
