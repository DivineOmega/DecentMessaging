package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection 
{
	private String dbConnectionString = "jdbc:h2:~/.decentmessaging/database;AUTO_SERVER=TRUE";
	private static Connection conn = null;
	
	public boolean setupSchema()
	{
		if (getConn()==null) return false;
		
		try
		{
			Statement stmt;
			String sql;
			
			stmt = getConn().createStatement();
			sql = "CREATE TABLE IF NOT EXISTS message ";
			sql += "(id INT PRIMARY KEY AUTO_INCREMENT, ";
			sql += "originally_received DATETIME, ";
			sql += "times_broadcast INT, ";
			sql += "decryption_attempted BOOLEAN DEFAULT FALSE, ";
			sql += "content_sha256 VARCHAR(255), ";
			sql += "content_size INT, ";
			sql += "iv BINARY, ";
			sql += "content_key BINARY, ";
			sql += "signature BINARY)";
			stmt.execute(sql);
			stmt.close();
			
			stmt = getConn().createStatement();
			sql = "CREATE TABLE IF NOT EXISTS personal ";
			sql += "(id INT PRIMARY KEY AUTO_INCREMENT, ";
			sql += "originally_received DATETIME, ";
			sql += "subject VARCHAR(255), ";
			sql += "publickey_modulus VARCHAR, ";
			sql += "publickey_exponent VARCHAR)";
			stmt.execute(sql);
			stmt.close();
			
			stmt = getConn().createStatement();
			sql = "CREATE TABLE IF NOT EXISTS node ";
			sql += "(id INT PRIMARY KEY AUTO_INCREMENT, ";
			sql += "host VARCHAR(255), ";
			sql += "port INT, ";
			sql += "last_seen DATETIME)";
			stmt.execute(sql);
			stmt.close();
			
			stmt = getConn().createStatement();
			sql = "CREATE TABLE IF NOT EXISTS privatekey ";
			sql += "(id INT PRIMARY KEY AUTO_INCREMENT, ";
			sql += "modulus VARCHAR, ";
			sql += "exponent VARCHAR)";
			stmt.execute(sql);
			stmt.close();
			
			stmt = getConn().createStatement();
			sql = "CREATE TABLE IF NOT EXISTS publickey ";
			sql += "(id INT PRIMARY KEY AUTO_INCREMENT, ";
			sql += "modulus VARCHAR, ";
			sql += "exponent VARCHAR)";
			stmt.execute(sql);
			stmt.close();
		}
		catch (SQLException e)
		{
			System.out.println(e.getLocalizedMessage());
			return false;
		}
		
		return true;
		
	}
	
	public boolean connect()
	{
		try 
		{
			Class.forName("org.h2.Driver");
			setConn(DriverManager.getConnection(dbConnectionString));
		} 
		catch (ClassNotFoundException e) 
		{
			System.out.println(e.getLocalizedMessage());
			return false;
		} 
		catch (SQLException e) 
		{
			System.out.println(e.getLocalizedMessage());
			return false;
		}
		return true;
	}
	
	public boolean disconnect()
	{
		try
		{
			getConn().close();
		} 
		catch (SQLException e) 
		{
			System.out.println(e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	public static Connection getConn() {
		return conn;
	}

	public static void setConn(Connection conn) {
		DatabaseConnection.conn = conn;
	}
}
