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

public PKI() {
}
	

// Temos que configurar o SSH ?


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
