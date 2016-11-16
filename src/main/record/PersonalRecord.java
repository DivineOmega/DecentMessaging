package main.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import main.DatabaseConnection;
import main.Main;

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
		String content_path = Main.storageDirectory+"personal"+System.getProperty("file.separator")+id;
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
	
	public boolean delete()
	{
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "delete from personal where id = ? limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, this.id);
			int rowsDeleted = stmt.executeUpdate();
			boolean fileDeleted = content_file.delete();
			
			if (fileDeleted == false || rowsDeleted != 1) {
				return false;
			} else {
				return true;
			}
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
}
