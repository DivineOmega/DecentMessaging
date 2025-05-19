package main.network;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

/**
 * Utility class for creating SSL contexts with a transient self-signed
 * certificate. A random password is used to secure the in-memory key store so
 * no fixed credentials are required.
 */
public class SSLContextFactory {

    /**
     * Creates a new SSLContext containing a self-signed certificate. Each call
     * generates a fresh key pair and password.
     */
    public static SSLContext createContext() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        X509Principal dn = new X509Principal("CN=DecentMessaging");
        certGen.setIssuerDN(dn);
        certGen.setSubjectDN(dn);
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 60000));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 31536000000L));
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        X509Certificate cert = certGen.generate(keyPair.getPrivate());

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        String password = generatePassword();
        char[] pwChars = password.toCharArray();
        ks.setKeyEntry("key", keyPair.getPrivate(), pwChars, new java.security.cert.Certificate[] { cert });

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, pwChars);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return ctx;
    }

    /**
     * Convenience method to retrieve a client socket factory using a fresh
     * context.
     */
    public static SSLSocketFactory newSocketFactory() throws Exception {
        return createContext().getSocketFactory();
    }

    /**
     * Convenience method to retrieve a server socket factory using a fresh
     * context.
     */
    public static SSLServerSocketFactory newServerSocketFactory() throws Exception {
        return createContext().getServerSocketFactory();
    }

    private static String generatePassword() {
        return new BigInteger(130, new SecureRandom()).toString(32);
    }
}
