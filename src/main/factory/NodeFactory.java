package main.factory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import main.DatabaseConnection;
import main.record.NodeRecord;

public abstract class NodeFactory 
{
	public static NodeRecord createNew(String host, int port)
	{
		
		int id = 0;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			
			String sql = "select id from node where host = ? and port = ? limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, host);
			stmt.setInt(2, port);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt("ID");
			}
			stmt.close();
			
			if (id==0)
			{
				sql = "insert into node set host = ?, port = ?, last_seen = NOW()";
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, host);
				stmt.setInt(2, port);
				stmt.executeUpdate();
				ResultSet generatedKeys = stmt.getGeneratedKeys();
				if (generatedKeys.next()) 
				{
				    id = generatedKeys.getInt(1);
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		return get(id);
	}
	
	public static NodeRecord getNodeToRelay()
	{
		if (Math.random()<0.10) {
			return NodeFactory.getRandomNode();
		}
		
		ArrayList<NodeRecord> recentNodes = NodeFactory.getRecentNodes();
		
		if (recentNodes.size()>0) {
			Collections.shuffle(recentNodes);
			return recentNodes.get(0);
		}
		
		return null;
	}
	
	public static ArrayList<NodeRecord> getRecentNodes()
	{
		ArrayList<NodeRecord> nodes = new ArrayList<NodeRecord>();
		int id = 0;
		NodeRecord node = null;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select id from node order by last_seen desc limit 10";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				id = rs.getInt("ID");
				node = NodeFactory.get(id);
				if (node!=null) nodes.add(node);
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return nodes;
	}
	
	public static ArrayList<NodeRecord> getRandomNodes()
	{
		ArrayList<NodeRecord> nodes = new ArrayList<NodeRecord>();
		int id = 0;
		NodeRecord node = null;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select id from node order by rand() limit 10";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
			{
				id = rs.getInt("ID");
				node = NodeFactory.get(id);
				if (node!=null) nodes.add(node);
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return nodes;
	}
	
	public static NodeRecord getRandomNode()
	{
		ArrayList<NodeRecord> nodes = getRandomNodes();
		
		if (nodes != null && nodes.size()>0) 
		{
			return nodes.get(0);
		}
		
		return null;
	}
	
	public static NodeRecord get(int id)
	{
		String host = null;
		int port = 0;
		Date last_seen = null;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select * from node where id = ? limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				host = rs.getString("HOST");
				port = rs.getInt("PORT");
				last_seen = rs.getDate("LAST_SEEN");
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (host==null || port==0|| last_seen==null)
		{
			return null;
		}
		else
		{
			return new NodeRecord(id, host, port, last_seen);
		}
		
	}
	
}
