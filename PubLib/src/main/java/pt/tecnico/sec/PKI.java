package pt.tecnico.sec;

import java.io.IOException;
import java.security.*;
import java.util.HashMap;


import javax.crypto.Cipher;

public class PKI {

	public static int KEYSIZE ;
	private static HashMap <String,PublicKey> KEYS = new HashMap <String,PublicKey>();// userID, Key
	private KeyStore keyStore;
	
public PKI(int keySize) {
	KEYSIZE = keySize ;
	try {
		this.keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	} catch (KeyStoreException e) {
		e.printStackTrace();
	}
}

public PublicKey getKey(String uID) throws Exception {
	if(!KEYS.containsKey(uID))
		throw new Exception("No such user");
	return KEYS.get(uID);
}

//public KeyPair createKeys(String userID) {
//    KeyPairGenerator keyGen;
//    KeyPair keyPair = null;
//	try {
//		keyGen = KeyPairGenerator.getInstance("RSA");
//		keyGen.initialize(KEYSIZE);
//
//		keyPair = keyGen.generateKeyPair();
//					
//		PublicKey pubKey = keyPair.getPublic();
//		KEYS.put(userID,pubKey);
//		
//		//keyStore.add()
//				
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		}
//		return keyPair;
//}

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
