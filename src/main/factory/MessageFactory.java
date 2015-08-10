package main.factory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import main.DatabaseConnection;
import main.record.MessageRecord;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public abstract class MessageFactory 
{	
	public static MessageRecord getBySha256(String sha256)
	{
		int id = 0;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select id from message where content_sha256 = ? limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, sha256);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt("ID");
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return get(id);
	}
	
	public static MessageRecord getMessageToDelete()
	{
		int id = 0;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select id from message order by times_broadcast desc, content_size desc, originally_received limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt("ID");
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return get(id);
	}
	
	public static MessageRecord getMessageToDecrypt()
	{
		int id = 0;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select id from message where decryption_attempted=false order by originally_received limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt("ID");
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return get(id);
	}
	
	public static MessageRecord getMessageToRelay()
	{
		int id = 0;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select id from message order by times_broadcast, content_size, originally_received limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				id = rs.getInt("ID");
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return get(id);
	}
	
	public static MessageRecord get(int id)
	{
		Date originally_received = null;
		int times_broadcast = 0;
		String content_sha256 = null;
		int content_size = 0;
		byte[] content_key = null;
		byte[] iv = null;
		byte[] signature = null;
		boolean decryption_attempted = false;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select * from message where id = ? limit 1";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				originally_received = rs.getTimestamp("ORIGINALLY_RECEIVED");
				times_broadcast = rs.getInt("TIMES_BROADCAST");
				decryption_attempted = rs.getBoolean("DECRYPTION_ATTEMPTED");
				content_sha256 = rs.getString("CONTENT_SHA256");
				content_size = rs.getInt("CONTENT_SIZE");
				iv = rs.getBytes("IV");
				content_key = rs.getBytes("CONTENT_KEY");
				signature = rs.getBytes("SIGNATURE");
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		if (originally_received==null || content_sha256==null || content_size==0 || content_key==null || iv==null || signature==null)
		{
			return null;
		}
		else
		{
			return new MessageRecord(id, originally_received, times_broadcast, decryption_attempted, content_sha256, content_size, iv, content_key, signature);
		}
		
	}
	
	public static MessageRecord encryptContentAndCreateNew(byte[] content, PublicKey pubKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, IOException, SignatureException, DataLengthException, IllegalStateException, InvalidCipherTextException
	{
		// START REGULAR JAVA CIPHER CODE
		/*
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		int keySize = 256;
	    kgen.init(keySize);
	    SecretKey key = kgen.generateKey();
	    byte[] aesKey = key.getEncoded();
	    SecretKeySpec aesKeySpec = new SecretKeySpec(aesKey, "AES");
	    Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		aesCipher.init(Cipher.ENCRYPT_MODE, aesKeySpec);
		byte[] encryptedContent = aesCipher.doFinal(content);
		byte[] iv = aesCipher.getIV();
		*/
		
		// START BOUNCY CASTLE CIPHER CODE
		PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
		
		byte[] aesKey = new byte[32];
		byte[] iv = new byte[aesCipher.getBlockSize()];
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.nextBytes(aesKey);
		secureRandom.nextBytes(iv);
		
		ParametersWithIV parameterIV = new ParametersWithIV(new KeyParameter(aesKey),iv);
		aesCipher.init(true, parameterIV);
		aesCipher.getBlockSize();
		int expectedOutputSize = aesCipher.getOutputSize(content.length);
        byte[] encryptedContent = new byte[expectedOutputSize];
		int outputSize = aesCipher.processBytes(content, 0, content.length, encryptedContent, 0);
		outputSize += aesCipher.doFinal(encryptedContent, outputSize);
		
        if(outputSize != expectedOutputSize)
        {
            byte[] tmp = new byte[outputSize];
            System.arraycopy(encryptedContent, 0, tmp, 0, outputSize);
            encryptedContent = tmp;
        }
		// END BOUNCY CASTLE CIPHER CODE
        
        Cipher rsaCipher = Cipher.getInstance("RSA");
		rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] encryptedKey = rsaCipher.doFinal(aesKey);
		
		return signAndCreateNew(iv, encryptedKey, encryptedContent);
	}
	
	public static MessageRecord signAndCreateNew(byte[] iv, byte[] encryptedKey, byte[] encryptedContent) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException, SignatureException
	{
		Signature rsaSig = Signature.getInstance("SHA256withRSA");
		rsaSig.initSign(PrivateKeyFactory.get(1).getKey());
		rsaSig.update(encryptedContent);
		byte[] signature = rsaSig.sign();
		
		return createNew(iv, encryptedKey, signature, encryptedContent);
	}

	public static MessageRecord createNew(byte[] iv, byte[] encryptedKey, byte[] signature, byte[] encryptedContent) throws IOException 
	{
		String content_sha256 = DigestUtils.sha256Hex(encryptedContent);
		int content_size = encryptedContent.length;
		
		int id = 0;
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "insert into message set originally_received = NOW(), times_broadcast = 0, content_sha256 = ?, content_size = ?, iv = ?, content_key = ?, signature = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, content_sha256);
			stmt.setInt(2, content_size);
			stmt.setBytes(3, iv);
			stmt.setBytes(4, encryptedKey);
			stmt.setBytes(5, signature);
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
			String content_path = System.getProperty("user.home")+System.getProperty("file.separator")+".decentmessaging"+System.getProperty("file.separator")+"message"+System.getProperty("file.separator")+content_sha256;
			FileOutputStream fos = new FileOutputStream(content_path);
			fos.write(encryptedContent);
			fos.close();
		}
		
		return get(id);
	}
	
}
