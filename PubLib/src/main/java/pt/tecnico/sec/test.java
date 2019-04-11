package pt.tecnico.sec;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

public class test {

	public static void main(String[] args) {
		eIDLib eid = new eIDLib();
		//eid.start();
		X509Certificate cert = null;
		/*
		try {
			//cert = PKI.generateCertificate("user1" , 7, "SHA1withRSA");
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		if(eid.verifySignature(eid.sign(cert,"teste"),"teste"))
			System.out.println("VALIDOU");
		else
			System.out.println("MACACO");
	}

}
