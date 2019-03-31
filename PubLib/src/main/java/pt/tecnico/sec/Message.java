package pt.tecnico.sec;

import java.io.Serializable;
import java.security.PublicKey;

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


 public Message(String id, String text, byte[] signature, Object key) {
     this.text = text;
     this.signature = signature;
     this.objects = key;//chave p.e
     this.id=id;
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
 
 public Object getObj() {
	 return objects;
 }
}