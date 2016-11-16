package main.record;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
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
import main.Main;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class MessageRecord 
{
	public int id;
	Date originally_received;
	int times_broadcast;
	boolean decryption_attempted;
	public String content_sha256;
	public int content_size;
	File content_file;
	public byte[] content_key;
	public byte[] iv;
	public byte[] signature;
	
	public MessageRecord(int id, Date originally_received, int times_broadcast, boolean decryption_attempted, String content_sha256, int content_size, byte[] iv, byte[] content_key, byte[] signature)
	{
		this.id = id;
		this.originally_received = originally_received;
		this.times_broadcast = times_broadcast;
		this.decryption_attempted = decryption_attempted;
		this.content_sha256 = content_sha256;
		this.content_size = content_size;
		String content_path = Main.storageDirectory+"message"+System.getProperty("file.separator")+content_sha256;
		this.content_file = new File(content_path);
		this.iv = iv;
		this.content_key = content_key;
		this.signature = signature;
	}
	
	public byte[] getContent() throws FileNotFoundException, IOException
	{
		if (!content_file.exists()) {
			return null;
		}
		
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
	
	public byte[] getDecryptedContent(PrivateKey privKey) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, DataLengthException, IllegalStateException, InvalidCipherTextException
	{
		
		Cipher rsaCipher = Cipher.getInstance("RSA");
		rsaCipher.init(Cipher.DECRYPT_MODE, privKey);
		byte[] decryptedKey = rsaCipher.doFinal(content_key);
		
		// START REGULAR JAVA CIPHER CODE
		/*
		Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		SecretKeySpec aesKeySpec = new SecretKeySpec(decryptedKey, "AES");
		aesCipher.init(Cipher.DECRYPT_MODE, aesKeySpec, new IvParameterSpec(iv));
		byte[] decryptedContent = aesCipher.doFinal(getContent());
		*/
		// END REGULAR JAVA CIPHER CODE
		
		// START BOUNCY CASTLE CIPHER CODE
		PaddedBufferedBlockCipher aesCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
		ParametersWithIV parameterIV = new ParametersWithIV(new KeyParameter(decryptedKey),iv);
		aesCipher.init(false, parameterIV);
		int expectedOutputSize = aesCipher.getOutputSize(content_size);
        byte[] decryptedContent = new byte[expectedOutputSize];
		int outputSize = aesCipher.processBytes(getContent(), 0, content_size, decryptedContent, 0);
		outputSize += aesCipher.doFinal(decryptedContent, outputSize);
		
        if(outputSize != expectedOutputSize)
        {
            byte[] tmp = new byte[outputSize];
            System.arraycopy(decryptedContent, 0, tmp, 0, outputSize);
            decryptedContent = tmp;
        }
        // END BOUNCY CASTLE CIPHER CODE
		
		return decryptedContent;
	}

	public void alterTimesBroadcast(int offset) 
	{
		if (times_broadcast>999999) times_broadcast = 0;
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "update message set times_broadcast = times_broadcast + ? where id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, offset);
			stmt.setInt(2, id);
			stmt.executeUpdate();
			times_broadcast += offset;
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setDecryptionAttempted(boolean decryption_attempted)
	{
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "update message set decryption_attempted = ? where id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setBoolean(1, decryption_attempted);
			stmt.setInt(2, id);
			stmt.executeUpdate();
			decryption_attempted = true;
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public boolean verifySignature(byte[] decryptedContent) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, FileNotFoundException, IOException 
	{
		String decryptedString = new String(decryptedContent, "UTF-8");
		String[] decryptedStrings = decryptedString.split("\n", 3);
		if (decryptedStrings.length!=3) return false;
		
		String senderAddress = decryptedStrings[0];
		BigInteger senderModulus = Main.getModulusFromDmAddress(senderAddress);
		BigInteger senderExponent = Main.getExponentFromDmAddress(senderAddress);
		
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(senderModulus, senderExponent);
		KeyFactory fact = KeyFactory.getInstance("RSA");
		PublicKey senderPublicKey = fact.generatePublic(keySpec);
		
		Signature rsaSig = Signature.getInstance("SHA256withRSA");
		rsaSig.initVerify(senderPublicKey);
		rsaSig.update(getContent());
		if (rsaSig.verify(signature)) return true;
		
		return false;
		
	}
	
	public static long getUsedSpace()
	{
		long usedSpace = 0;
		
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "select sum(content_size) as spaceused from message";
			PreparedStatement stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
			{
				usedSpace = rs.getLong("SPACEUSED");
			}
			stmt.close();
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
		return usedSpace;
	}

	public boolean deleteContentFile()
	{
		try
		{
			Connection conn = DatabaseConnection.getConn();
			String sql = "update message set content_size = 0 where id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, id);
			stmt.executeUpdate();
			if (!content_file.delete()) return false;
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
