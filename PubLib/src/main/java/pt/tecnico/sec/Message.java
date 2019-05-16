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
private signature sellSig;
private final signature writeSignature;
private final String id;
private final signature buyerSignature;
private final signature certsig;
private final Recorded rec;
private byte[] encodedhash;

 public Message(String id, String text, signature[] signatures, Recorded recorded, signature certsig) {
     this.text = text;
     this.sellSig = signatures[0];
//     this.key = key;
     this.writeSignature=signatures[1];
     this.id=id;
     this.buyerSignature=signatures[2];
     this.certsig = certsig;
     this.rec = recorded;
     hash();
 }
 

 
 
 
 
 public Message(String id, String text, signature[] signatures, Recorded recorded) {
	 this.id=id;
	 this.text=text;
	 this.certsig=null;
     this.sellSig = signatures[0];
     this.writeSignature=signatures[1];
     this.buyerSignature=signatures[2];
     this.rec = recorded;
//	 this.rec = new Recorded("",-1,-1);
	 hash();

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
 
 public signature getSellSig() {
	 return sellSig;
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
 
 public signature getCertSig() {
	 return certsig;
 }
 
 public void setSignature(signature sig) {
	 this.signature = sig;
	 
 }
 
}