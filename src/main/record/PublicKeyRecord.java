package main.record;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class PublicKeyRecord 
{
	int id;
	public BigInteger modulus;
	public BigInteger exponent;
	
	public PublicKeyRecord(int id, BigInteger modulus, BigInteger exponent)
	{
		this.id = id;
		this.modulus = modulus;
		this.exponent = exponent;
	}
	
	public PublicKey getKey() throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory fact = KeyFactory.getInstance("RSA");
		PublicKey key = fact.generatePublic(keySpec);
	    return key;
	}
	
	
}
