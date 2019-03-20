package pt.tecnico.sec;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.HashMap;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PKI {

	private final int keySize = 1024;
	private HashMap <String,PublicKey> keys = new HashMap <String,PublicKey>();
	
public KeyPair createKeys(String keyPath) throws GeneralSecurityException, IOException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(this.keySize);
    KeyPair keys = keyGen.generateKeyPair();
    
    return keys;
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
