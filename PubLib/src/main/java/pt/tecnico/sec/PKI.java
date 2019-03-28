package pt.tecnico.sec;

import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.HashMap;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class PKI {

	public static int KEYSIZE ;
	private static HashMap <String,PublicKey> KEYS = new HashMap <String,PublicKey>();// userID, Key
	public static KeyStore KEYSTORE;
	
public PKI(int keySize) {
	KEYSIZE = keySize ;
	try {
		this.KEYSTORE = KeyStore.getInstance(KeyStore.getDefaultType());
	} catch (KeyStoreException e) {
		e.printStackTrace();
	}
}

public PublicKey getKey(String uID) throws Exception {
	if(!KEYS.containsKey(uID))
		throw new Exception("No such user");
	return KEYS.get(uID);
}

public KeyPair createKeys(String userID) {
    KeyPairGenerator keyGen;
    KeyPair keyPair = null;
	try {
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(KEYSIZE);

		keyPair = keyGen.generateKeyPair();
					
		PublicKey pubKey = keyPair.getPublic();
		KEYS.put(userID,pubKey);

		//KEYSTORE.setKeyEntry(userID, keyPair.getPrivate(), );
		
		char[] password = new char[] {'a','b'};
		KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry((SecretKey) keyPair.getPrivate());
		KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(password);
		KEYSTORE.setEntry(userID,skEntry,protParam);
				
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyPair;
}

public byte[] encrypt(PrivateKey privateKey, String message) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");  
    cipher.init(Cipher.ENCRYPT_MODE, privateKey);  

    return cipher.doFinal(message.getBytes());  
}

public byte[] decrypt(PublicKey publicKey, byte [] encrypted) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA");  
    cipher.init(Cipher.DECRYPT_MODE, publicKey);
    
    return cipher.doFinal(encrypted);
}

public String teste() {
	return "Ok";
}
    


}
