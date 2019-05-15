package pt.tecnico.sec;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class test {

	public static void main(String[] args) {
		eIDLib eid = new eIDLib();
		//eid.start();
		X509Certificate cert = null;
		
		cert = eid.getCert();
		
		System.out.println("OLA "+cert);
		
		if(eid.verifySignature(eid.sign(cert,"teste"),"teste"))
			System.out.println("VALIDOU");
		else
			System.out.println("MATEMATICO");
	}

}
