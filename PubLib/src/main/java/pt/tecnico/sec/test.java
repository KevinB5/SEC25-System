package pt.tecnico.sec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.xml.bind.DatatypeConverter;

public class test {

	public static void main(String[] args) {
//		JSONGood json = new JSONGood();
//		System.out.println(json.getGoodList("notary4"));
//		eIDLib eid = new eIDLib();
//		//eid.start();
//		X509Certificate cert = null;
//		
//		cert = eid.getCert();
//		
//		System.out.println("OLA "+cert);
//		
//		if(eid.verifySignature(eid.sign(cert,"teste"),"teste"))
//			System.out.println("VALIDOU");
//		else
//			System.out.println("MATEMATICO");
		
	Pair powHash= powHash("Hello World!");
	System.out.println(verifyHash(powHash.getValue()+"",powHash.getKey()+""));
		
	}
	static String hashLimit ="0000";
	public static boolean verifyHash(String content,String hashValue) {
		content = content+hashValue;
		System.out.println(content);
		MessageDigest digest;
		byte[] hash = null;
		for(int i = 0 ; i<5;i++) {
		try {
			digest = MessageDigest.getInstance("SHA-256");
			hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
			System.out.println(hash);
			content = DatatypeConverter.printHexBinary(hash);
			content = content.substring(0,4);
			System.out.println(content);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		}
		if(content.equals(hashLimit))
			return true;
		return false;
	}
	
	
	public static Pair powHash(String content) {
		/* Returns string i such that content+i hashes to a string with hashLimit in the beginning */
		MessageDigest digest;
		byte[] hash = null;
		String hashString= null;
		int i=0;
		do {
			hashString = content+i;
			try {
				digest = MessageDigest.getInstance("SHA-256");
				hash = digest.digest(hashString.getBytes(StandardCharsets.UTF_8));
				content = DatatypeConverter.printHexBinary(hash);
				hashString = hashString.substring(0,4);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			i++;
		}
		while(!hashString.equals(hashLimit)); 
		return new Pair(i,content);
	}

}
