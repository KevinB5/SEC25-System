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
		eIDLib eid = new eIDLib();
		//eid.start();
		X509Certificate cert = null;
		
		cert = eid.getCert();
		
		System.out.println("OLA "+cert);
		
		if(eid.verifySignature(eid.sign(cert,"teste"),"teste"))
			System.out.println("VALIDOU");
		else
			System.out.println("MATEMATICO");
		
//	try {
//		digest = MessageDigest.getInstance("SHA-1");
//	} catch (NoSuchAlgorithmException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	String text= "Hello World!";
//	
//	try {
//		digest2 = MessageDigest.getInstance("SHA-1");
//	} catch (NoSuchAlgorithmException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	
//	
//	System.out.println(verifyHash(powHash(text),text));
		
	}
	static MessageDigest digest;
	static MessageDigest digest2;
	static String hashLimit ="0000";
	
	
	
	
	public static boolean verifyHash(String hashValue,String content) {
		String testContent = content+hashValue;
//		System.out.println(testContent);
		byte[] hash = null;
		for(int i = 0 ; i<5;i++) {
			content = testContent;
//			System.out.println(content);
			hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
//			System.out.println(hash);
			content = DatatypeConverter.printHexBinary(hash);
			content = content.substring(0,4);
//			System.out.println(content);
		}
		if(content.equals(hashLimit))
			return true;
		return false;
	}
	
	
	public static String powHash(String content) {
		/* Returns string i such that content+i hashes to a string with hashLimit in the beginning */
		String originalContent = content;
		byte[] hash = null;
		String hashString= null;
		int i=0;
		do {
			i++;
			hashString = originalContent+i;
//			System.out.println(hashString);
			hash = digest2.digest(hashString.getBytes(StandardCharsets.UTF_8));
			content = DatatypeConverter.printHexBinary(hash);
			hashString = content.substring(0,4);
//				System.out.println("hashString: "+hashString);
		}
		while(!hashString.equals(hashLimit)); 
//		System.out.println(i);
		return ""+i;
	}

}
