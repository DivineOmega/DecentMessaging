package main.factory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import main.DatabaseConnection;
import main.Main;
import main.record.PersonalRecord;

import org.apache.commons.codec.binary.Base64;

public abstract class PersonalFactory 
{	
	
	public static PersonalRecord createNew(byte[] decryptedContent) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException
	{	
		String decryptedString = new String(decryptedContent, "UTF-8");
		String[] decryptedStrings = decryptedString.split("\n", 3);
		if (decryptedStrings.length!=3) return null;
		
		String senderAddress = decryptedStrings[0];
		String senderModulus = Main.getModulusFromDmAddress(senderAddress).toString();
		String senderExponent = Main.getExponentFromDmAddress(senderAddress).toString();
		
		int id = 0;
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "insert into personal set originally_received = NOW(), subject = ?, publickey_modulus = ?, publickey_exponent = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, decryptedStrings[1]);
			stmt.setString(2, senderModulus);
			stmt.setString(3, senderExponent);
			stmt.executeUpdate();
			ResultSet generatedKeys = stmt.getGeneratedKeys();
			if (generatedKeys.next()) 
			{
			    id = generatedKeys.getInt(1);
			}
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (id!=0)
		{
			String content_path = Main.storageDirectory+"personal"+System.getProperty("file.separator")+id;
			FileOutputStream fos = new FileOutputStream(content_path);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
			out.write(decryptedStrings[2]);
			out.close();
			fos.close();
		}
		
		return get(id);
	}
	
	public static PersonalRecord get(int id)
	{
		Date originally_received = null;
		String subject = null;
		String publickey_modulus = null;
		String publickey_exponent = null;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select * from personal where id = ? limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				originally_received = rs.getTimestamp("ORIGINALLY_RECEIVED");
				subject = rs.getString("SUBJECT");
				publickey_modulus = rs.getString("PUBLICKEY_MODULUS");
				publickey_exponent = rs.getString("PUBLICKEY_EXPONENT");
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (originally_received==null || subject==null || publickey_modulus==null || publickey_exponent==null)
		{
			return null;
		}
		else
		{
			return new PersonalRecord(id, originally_received, subject, publickey_modulus, publickey_exponent);
		}
		
	}
	
	public static ArrayList<Integer> getIDsAfterTimestamp(Timestamp timestamp)
	{
		ArrayList<Integer> ids = new ArrayList<Integer>();
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select * from personal where originally_received > ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setTimestamp(1, timestamp);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				ids.add(rs.getInt("ID"));
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return ids;
		
	}
	
}
