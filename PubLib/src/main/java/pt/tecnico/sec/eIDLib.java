package pt.tecnico.sec;

import pteidlib.PTEID_ADDR;
import pteidlib.PTEID_Certif;
import pteidlib.PTEID_ID;
import pteidlib.PTEID_PIC;
import pteidlib.PTEID_Pin;
import pteidlib.PTEID_TokenInfo;
import pteidlib.PteidException;
import pteidlib.pteid;

import sun.security.pkcs11.wrapper.CK_ATTRIBUTE;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_MECHANISM;
import sun.security.pkcs11.wrapper.CK_SESSION_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Constants;
import sun.security.pkcs11.wrapper.PKCS11Exception;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

public class eIDLib{
	
	private static Signature sig;
	private static X509Certificate cert;
	
 
	public static X509Certificate getCert() {
		return cert;
	}

	// Falta buscar as chaves RSA e assinar os objectos propriamente
	public eIDLib() {
		try {
			System.out.println("            //Load the PTEidlibj");
			
			String path = System.getProperty("user.dir")+"\\Temp\\";
			
			path = path.replaceAll("\\\\","/");
			System.out.println(path);
	        System.setProperty("java.library.path", path);
	        Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
	        fieldSysPath.setAccessible(true);
	        fieldSysPath.set(null, null);
	        System.out.println(System.getProperty("java.library.path"));
	        System.loadLibrary("pteidlibj");
	        pteid.Init(""); // Initializes the eID Lib
	        pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
	        cert = getCertFromByteArray(getCertificateInBytes(0));
			sig = Signature.getInstance(cert.getSigAlgName());
			System.out.println(cert.getSigAlgName());
			
		} catch (NoSuchAlgorithmException | CertificateException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PteidException e) {
			CertificateFactory cf;
			try {
				cf = CertificateFactory.getInstance("X.509");
				cert = (X509Certificate)cf.generateCertificate(new FileInputStream(System.getProperty("user.dir")+"/test.cert"));
				sig = Signature.getInstance(cert.getSigAlgName());
				System.out.println("2 "+cert);
			} catch (CertificateException | FileNotFoundException | NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		System.out.println(cert);
//			e.printStackTrace();
		}
		
	}
//	public void start() {
//		 try
//	        {
//	            
////	        System.out.println("            //Load the PTEidlibj");
////	        System.setProperty("java.library.path", "/usr/local/lib/");
////	        Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
////	        fieldSysPath.setAccessible(true);
////	        fieldSysPath.set(null, null);
////	        System.loadLibrary("pteidlibj");
////	        pteid.Init(""); // Initializes the eID Lib
////	        pteid.SetSODChecking(false); // Don't check the integrity of the ID, address and photo (!)
//
//	        PKCS11 pkcs11;
//	        String osName = System.getProperty("os.name");
//	        String javaVersion = System.getProperty("java.version");
//	    
//	        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
//	     
//	        String libName = "libpteidpkcs11.so";
//	       
//	        
//	        X509Certificate cert=getCertFromByteArray(getCertificateInBytes(0));
//	        
//	        //Código de assinar
////	        Signature sig = Signature.getInstance(getCertFromByteArray(getCertificateInBytes(0)).getSigAlgName());
//	        sig.initVerify(cert);
//	        
//	        if (-1 != osName.indexOf("Windows"))
//                libName = "pteidpkcs11.dll";
//            else if (-1 != osName.indexOf("Mac"))
//                libName = "pteidpkcs11.dylib";
//            Class pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
//            if (javaVersion.startsWith("1.5."))
//            {
//                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
//                pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, null, false });
//            }
//            else
//            {
//                Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
//                pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
//            }
//	        
//            //Open the PKCS11 session
//            System.out.println("            //Open the PKCS11 session");
//            long p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
//            
//            // Token login 
//            System.out.println("            //Token login");
//            pkcs11.C_Login(p11_session, 1, null);
//            CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);
//            
//            // Get available keys
//            System.out.println("            //Get available keys");
//            CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
//            attributes[0] = new CK_ATTRIBUTE();
//            attributes[0].type = PKCS11Constants.CKA_CLASS;
//            attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);
//            
//            pkcs11.C_FindObjectsInit(p11_session, attributes);
//            long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);
//            
//            // points to auth_key
//            System.out.println("            //points to auth_key. No. of keys:"+keyHandles.length);
//            
//            long signatureKey = keyHandles[0];		//test with other keys to see what you get
//            pkcs11.C_FindObjectsFinal(p11_session);
//            
//            
//            // initialize the signature method
//            System.out.println("            //initialize the signature method");
//            CK_MECHANISM mechanism = new CK_MECHANISM();
//            mechanism.mechanism = PKCS11Constants.CKM_SHA1_RSA_PKCS;
//            mechanism.pParameter = null;
//            pkcs11.C_SignInit(p11_session, mechanism, signatureKey);
//	        
//            
//            //Assinatura para usar 
//            //Lab 2 editar
//            
//            byte[] signature = pkcs11.C_Sign(p11_session, "data".getBytes(Charset.forName("UTF-8")));
//
//            
//            //Verificar assinature
//            sig.update("data".getBytes(Charset.forName("UTF-8")));
//           
//            
//            if(sig.verify(signature)) {
//            	System.out.println("            //Valid");
//            }else {
//            	System.out.println("            //Not valid");
//            }
//            
//            pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); //OBRIGATORIO Termina a eID Lib
//	        }  catch (Throwable e)
//	        {
//	            System.out.println("[Catch] Exception: " + e.getMessage());
//	            e.printStackTrace();
//		    }
//	}
	
	public byte[] sign(X509Certificate cert,String data) {
		PKCS11 pkcs11 = null;
        String osName = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        long p11_session = 0;
        byte[] signature = null;
    
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
     
        String libName = "libpteidpkcs11.so";
		 try {
			sig.initVerify(cert);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        
	        if (-1 != osName.indexOf("Windows"))
             libName = "pteidpkcs11.dll";
         else if (-1 != osName.indexOf("Mac"))
             libName = "pteidpkcs11.dylib";
         Class pkcs11Class;
		try {
			pkcs11Class = Class.forName("sun.security.pkcs11.wrapper.PKCS11");
         if (javaVersion.startsWith("1.5."))
         {
             Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
             pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, null, false });
         }
         else
         {
             Method getInstanceMethode = pkcs11Class.getDeclaredMethod("getInstance", new Class[] { String.class, String.class, CK_C_INITIALIZE_ARGS.class, boolean.class });
             pkcs11 = (PKCS11)getInstanceMethode.invoke(null, new Object[] { libName, "C_GetFunctionList", null, false });
         }
	        
         //Open the PKCS11 session
         System.out.println("            //Open the PKCS11 session");
         p11_session = pkcs11.C_OpenSession(0, PKCS11Constants.CKF_SERIAL_SESSION, null, null);
         
         // Token login 
         System.out.println("            //Token login");
         pkcs11.C_Login(p11_session, 1, null);
         CK_SESSION_INFO info = pkcs11.C_GetSessionInfo(p11_session);
         
         // Get available keys
         System.out.println("            //Get available keys");
         CK_ATTRIBUTE[] attributes = new CK_ATTRIBUTE[1];
         attributes[0] = new CK_ATTRIBUTE();
         attributes[0].type = PKCS11Constants.CKA_CLASS;
         attributes[0].pValue = new Long(PKCS11Constants.CKO_PRIVATE_KEY);
         
         pkcs11.C_FindObjectsInit(p11_session, attributes);
         long[] keyHandles = pkcs11.C_FindObjects(p11_session, 5);
         
         // points to auth_key
         System.out.println("            //points to auth_key. No. of keys:"+keyHandles.length);
         
         long signatureKey = keyHandles[0];		//test with other keys to see what you get
         pkcs11.C_FindObjectsFinal(p11_session);
         
         
         // initialize the signature method
         System.out.println("            //initialize the signature method");
         CK_MECHANISM mechanism = new CK_MECHANISM();
         mechanism.mechanism = PKCS11Constants.CKM_SHA256_RSA_PKCS;
         mechanism.pParameter = null;
         pkcs11.C_SignInit(p11_session, mechanism, signatureKey);
	        
         System.out.println("ASSINANDO: "+ data.getBytes(Charset.forName("UTF-8")));
         signature = pkcs11.C_Sign(p11_session, data.getBytes(Charset.forName("UTF-8")));
         
         //Assinatura para usar 
         //Lab 2 editar
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PKCS11Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return signature;
         
	}
	
	public static  byte[] getCertificateInBytes(int n) {
        byte[] certificate_bytes = null;
        try {
            PTEID_Certif[] certs = pteid.GetCertificates();
            System.out.println("Number of certs found: " + certs.length);
            int i = 0;
	    for (PTEID_Certif cert : certs) {
                System.out.println("-------------------------------\nCertificate #"+(i++));
                System.out.println(cert.certifLabel);
            }
            
            certificate_bytes = certs[n].certif; //gets the byte[] with the n-th certif
            
            //pteid.Exit(pteid.PTEID_EXIT_LEAVE_CARD); // OBRIGATORIO Termina a eID Lib
        } catch (PteidException e) {
            //e.printStackTrace();
        	try {
        		CertificateFactory cf = CertificateFactory.getInstance("X.509");
        		cert = (X509Certificate)cf.generateCertificate(new FileInputStream(System.getProperty("user.dir")+"/test.cert"));
        		sig = Signature.getInstance(cert.getSigAlgName());
        		System.out.println(cert.getSigAlgName());
        		System.out.println(cert);
        	}catch(Exception ex) {
        		ex.printStackTrace();
        	}
        }
        return certificate_bytes;
    }
	
	 public static X509Certificate getCertFromByteArray(byte[] certificateEncoded) throws CertificateException{
	        CertificateFactory f = CertificateFactory.getInstance("X.509");
	        InputStream in = new ByteArrayInputStream(certificateEncoded);
	        X509Certificate cert = (X509Certificate)f.generateCertificate(in);
	        return cert;
	  }
	 
	 public boolean verifySignature(byte[] signature,String data) {
		//Verificar assinature
         try {
			sig.update(data.getBytes(Charset.forName("UTF-8")));
			System.out.println(data.getBytes(Charset.forName("UTF-8")));
         
	         if(sig.verify(signature)) {
	        	 return true;
	         }
         
         } catch (SignatureException e) {
        	 // TODO Auto-generated catch block
        	 e.printStackTrace();
         }
         return false;
	 }
	 
	 
	 
	// public byte[] sign( p11_session, String text))
	 
	 
}
