package pt.tecnico.sec;

import java.io.Serializable;
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
private final signature signature;
private final signature writeSignature;
private final String id;
private final signature buyerSignature;
private final X509Certificate cert;
private final Recorded rec;


 public Message(String id, String text, signature[] signatures, Recorded recorded, X509Certificate cert) {
     this.text = text;
     this.signature = signatures[0];
//     this.key = key;
     this.writeSignature=signatures[1];
     this.id=id;
     this.buyerSignature=signatures[2];
     this.cert = cert;
     this.rec = recorded;
 }
 
 
 
 public String getID() {
	 return id;
 }


 public String getText() {
     return text;
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
}