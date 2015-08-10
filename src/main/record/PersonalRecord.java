package main.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Date;

public class PersonalRecord 
{
	int id;
	public Date originally_received;
	public String subject;
	File content_file;
	public BigInteger publickey_modulus;
	public BigInteger publickey_exponent;
	
	public PersonalRecord(int id, Date originally_received, String subject, String publickey_modulus, String publickey_exponent)
	{
		this.id = id;
		this.originally_received = originally_received;
		this.subject = subject;
		this.publickey_modulus = new BigInteger(publickey_modulus);
		this.publickey_exponent = new BigInteger(publickey_exponent);
		String content_path = System.getProperty("user.home")+System.getProperty("file.separator")+".decentmessaging"+System.getProperty("file.separator")+"personal"+System.getProperty("file.separator")+id;
		this.content_file = new File(content_path);
	}
	
	public byte[] getContent() throws FileNotFoundException, IOException
	{
		InputStream is = new FileInputStream(content_file);
	    byte[] bytes = new byte[(int) content_file.length()];
	    int offset = 0;
	    int numRead = 0;
	    
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) 
	    {
	        offset += numRead;
	    }

	    is.close();
	    return bytes;
	}
	
	public String getContentAsString() throws UnsupportedEncodingException, FileNotFoundException, IOException
	{
		return new String(getContent(), "UTF-8");
	}
	
}
