package main.factory;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.DatabaseConnection;
import main.record.PublicKeyRecord;

public abstract class PublicKeyFactory 
{	
	public static PublicKeyRecord get(int id)
	{
		BigInteger modulus = null;
		BigInteger exponent = null;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select * from publickey where id = ? limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				modulus = new BigInteger(rs.getString("MODULUS"));
				exponent = new BigInteger(rs.getString("EXPONENT"));
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (modulus==null || exponent==null)
		{
			return null;
		}
		else
		{
			return new PublicKeyRecord(id, modulus, exponent);
		}
		
	}
	
	public static PublicKeyRecord createNew(KeyPair kp) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
		
		int id = 0;
		
		try 
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "insert into publickey modulus = ?, exponent = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			
				stmt.setString(1, pub.getModulus().toString());
			
			stmt.setString(2, pub.getPublicExponent().toString());
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
		
		return get(id);
	}
	
	public static PublicKeyRecord createNew(int id, KeyPair kp) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPublicKeySpec pub = fact.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "insert into publickey set id = ?, modulus = ?, exponent = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.setString(2, pub.getModulus().toString());
			stmt.setString(3, pub.getPublicExponent().toString());
			stmt.executeUpdate();
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return get(id);
	}
	
}
