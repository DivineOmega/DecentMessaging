package main.record;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;

public class PrivateKeyRecord 
{
	int id;
	BigInteger modulus;
	BigInteger exponent;
	
	public PrivateKeyRecord(int id, BigInteger modulus, BigInteger exponent)
	{
		this.id = id;
		this.modulus = modulus;
		this.exponent = exponent;
	}
	
	public PrivateKey getKey() throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(modulus, exponent);
		KeyFactory fact = KeyFactory.getInstance("RSA");
	    PrivateKey key = fact.generatePrivate(keySpec);
	    return key;
	}
	
}
