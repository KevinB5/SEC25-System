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
private final byte[] signature;
private final PublicKey key;
private final String id;
private final byte[] buyerSignature;
private final X509Certificate cert;

 public Message(String id, String text, byte[] signature, byte[] buyerSignature, PublicKey key, X509Certificate cert) {
     this.text = text;
     this.signature = signature;
     this.key = key;
     this.id=id;
     this.buyerSignature=buyerSignature;
     this.cert = cert;
 }
 
 public Message(String id, String text, byte[] signature, HashMap<String,Object> secParameters ) {
     this.text = text;
     this.signature = signature;
     this.id=id;
     this.buyerSignature = (byte[]) secParameters.get("signature");
     this.cert = (X509Certificate) secParameters.get("certificate");
     this.key = (PublicKey) secParameters.get("key");
 }
 
 public Message(String id, String text, byte[] signature) {
     this.text = text;
     this.signature = signature;
     this.id=id;
     this.buyerSignature = null;
     this.cert = null;
     this.key = null;
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
 
 public PublicKey getObj() {
	 return key;
 }
}