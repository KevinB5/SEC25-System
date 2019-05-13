package pt.tecnico.sec;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

//must implement Serializable in order to be sent
public class Message implements Serializable{
 /**
	 * 
	 */
	private static final long serialVersionUID = 928168850408082591L;
	
private final String text;
private signature signature;
private final signature writeSignature;
private final String id;
private final signature buyerSignature;
private final X509Certificate cert;
private final Recorded rec;
private byte[] encodedhash;

 public Message(String id, String text, signature[] signatures, Recorded recorded, X509Certificate cert) {
     this.text = text;
     this.signature = signatures[0];
//     this.key = key;
     this.writeSignature=signatures[1];
     this.id=id;
     this.buyerSignature=signatures[2];
     this.cert = cert;
     this.rec = recorded;
     hash();
 }
 
 public Message(String id, String text, signature[] signatures) {
	 this.id=id;
	 this.text=text;
	 this.cert=null;
     this.signature = signatures[0];
//   this.key = key;
   this.writeSignature=signatures[1];
	 this.buyerSignature=null;
	 this.rec = null;

 }
 
 private void hash() {
	 String fullMsg = id + text + rec.getState() + rec.getCounter() + rec.getTS();
	 try {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		encodedhash=digest.digest(fullMsg.getBytes(StandardCharsets.UTF_8));
		
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

 }
 
 
 
 public String getID() {
	 return id;
 }


 public String getText() {
     return text;
 }
 
 public byte[] getHash() {
	 return this.encodedhash;
 }
 
 public signature getSig() {
	 return signature;
 }
 
 public signature buyerSignature() {
	 return buyerSignature;
 }
 
 public Recorded getRec() {
	 return rec;
 }
 public signature getWriteSignature() {
	 return writeSignature;
 }
 
 public X509Certificate getCertificate() {
	 return cert;
 }
 
 public void setSignature(signature sig) {
	 this.signature = sig;
	 
 }
 
}