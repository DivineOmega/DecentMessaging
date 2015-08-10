package main.record;

import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import main.DatabaseConnection;

public class NodeRecord 
{
	int id;
	public String host;
	public int port;
	Date last_seen;
	
	public NodeRecord(int id, String host, int port, Date last_seen)
	{
		this.id = id;
		this.host = host;
		this.port = port;
		this.last_seen = last_seen;
	}
	
	public static boolean deleteByHostAndPort(String host, int port)
	{
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "delete from node where host = ? and port = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, host);
			stmt.setInt(2, port);
			if (stmt.executeUpdate()==0) return false;
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	public static void updateLastSeenBySocket(Socket socket) 
	{
		String host1 = socket.getInetAddress().getHostAddress();
		String host2 = socket.getInetAddress().getHostName();
		int port = socket.getPort();
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "update node set last_seen = NOW() where (host = ? or host = ?) and port = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, host1);
			stmt.setString(2, host2);
			stmt.setInt(3, port);
			stmt.executeUpdate();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
