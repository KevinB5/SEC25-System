package pt.tecnico.sec;

import java.io.Serializable;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

//must implement Serializable in order to be sent
public class Message implements Serializable{
 /**
	 * 
	 */
	private static final long serialVersionUID = 928168850408082591L;
	
private final String text;
private final byte[] signature;
private final Object objects;
private final String id;
private final byte[] buyerSignature;
private final X509Certificate cert;

 public Message(String id, String text, byte[] signature, byte[] buyerSignature, Object key, X509Certificate cert) {
     this.text = text;
     this.signature = signature;
     this.objects = key;
     this.id=id;
     this.buyerSignature=buyerSignature;
     this.cert = cert;
 }
 
 public String getID() {
	 return id;
 }


 public String getText() {
     return text;
 }
 
 public byte[] getSig() {
	 return signature;
 }
 
 public byte[] buyerSignature() {
	 return buyerSignature;
 }
 
 public Object getObj() {
	 return objects;
 }
}