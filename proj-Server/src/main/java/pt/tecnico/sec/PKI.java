package pt.tecnico.sec;

import java.io.IOException;
import java.security.*;
import java.util.HashMap;


import javax.crypto.Cipher;

public class PKI {

	private int keySize ;
	private HashMap <String,PublicKey> keys = new HashMap <String,PublicKey>();
	
public PKI(int keySize) {
	this.keySize = keySize ;
}
	

// Temos que configurar o SSH ?
public KeyPair createKeys(String userID) {
    KeyPairGenerator keyGen;
    KeyPair keyPair = null;
	try {
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(this.keySize);
		keyPair = keyGen.generateKeyPair();
		
		PublicKey pubKey = keyPair.getPublic();
		keys.put(userID,pubKey);
		return keyPair;
	} catch (NoSuchAlgorithmException e) {
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
    

}
