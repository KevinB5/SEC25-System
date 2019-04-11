package pt.tecnico.sec;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import pt.tecnico.sec.eIDLib;

public class test {
	private static eIDLib eid;

	public static void main(String[] args) {
		start();
	}
	
	private static void start() {
		eid = new eIDLib();
		X509Certificate cert = null; 
         try {
			cert=eIDLib.getCertFromByteArray(eIDLib.getCertificateInBytes(0));
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(eid.verifySignature(eid.sign(cert,"teste"),"teste"))
			System.out.println("VALIDOU");
		else
			System.out.println("MACACO");

	}

}
