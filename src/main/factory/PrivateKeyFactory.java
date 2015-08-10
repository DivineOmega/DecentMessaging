package main.factory;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import main.DatabaseConnection;
import main.record.PrivateKeyRecord;

public abstract class PrivateKeyFactory 
{
	
	public static PrivateKeyRecord get(int id)
	{
		BigInteger modulus = null;
		BigInteger exponent = null;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select * from privatekey where id = ? limit 1";
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
			return new PrivateKeyRecord(id, modulus, exponent);
		}
		
	}
	
	public static PrivateKeyRecord createNew(KeyPair kp) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);
		
		int id = 0;
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "insert into privatekey modulus = ?, exponent = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, priv.getModulus().toString());
			stmt.setString(2, priv.getPrivateExponent().toString());
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
	
	public static PrivateKeyRecord createNew(int id, KeyPair kp) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		KeyFactory fact = KeyFactory.getInstance("RSA");
		RSAPrivateKeySpec priv = fact.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);

		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "insert into privatekey set id = ?, modulus = ?, exponent = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.setString(2, priv.getModulus().toString());
			stmt.setString(3, priv.getPrivateExponent().toString());
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