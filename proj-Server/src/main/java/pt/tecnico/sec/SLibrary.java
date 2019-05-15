package pt.tecnico.sec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import pt.tecnico.sec.App.ConnectClient;

public class SLibrary {
	private HashMap<String,ObjectOutputStream> sockets = new HashMap<String, ObjectOutputStream>();
	private Notary notary;
	private static final String IP = "127.0.0.1";

	
	public SLibrary(Notary not) {
		notary = not;
	}
	
    public void connect(String userID, int Uport) {
		try {
			Socket clientSocket = new Socket(IP, Uport);
			System.out.println("connected to server at port: "+ Uport);
			this.sockets.put(userID, new ObjectOutputStream(clientSocket.getOutputStream()));
			
		}catch(ConnectException cnn) {
			System.out.println(userID + " is not connected");
			System.out.println(cnn.getMessage());
		}catch(IOException ie) {
			ie.printStackTrace();
		}
	}
	
	public void sendMessage(String uID, Message msg) throws Exception {
		ExecutorService executor = Executors.newSingleThreadExecutor();

		executor.submit(new Sender(uID, msg));
	}
	
	private class Sender implements Runnable{
		String uID;
		Message msg;
		
		private Sender(String uID, Message msg) {
			this.uID=uID;
			this.msg=msg;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			ObjectOutputStream outU= null;
			 ObjectInputStream inU=null;
			 Message resp= null;
			 try {
				 System.out.println("Sending message to "+ uID);
				 
		         ReadWriteLock lock = new ReentrantReadWriteLock();
		         
		         
			 	
				 outU = sockets.get(uID);
				 if(outU == null)
					 throw new Exception("Must connect to user "+ uID+" first");
				
				 //outU = new ObjectOutputStream(clientSocket.getOutputStream());
					//inU= new ObjectInputStream(clientSocket.getInputStream());
				

				    lock.writeLock().lock();
				    try {
					outU.writeObject(msg);
			 		//outU.reset();

					outU.flush();

				    } catch (Exception e) {
							e.printStackTrace();
				    }finally {
							//liberta assim que terminar a escrita
							lock.writeLock().unlock();
					}
			 		
			 		//outU.writeObject(msg);
			 		//
			 		
					//
			 		//resp = (Message) inU.readObject();

				} catch (IOException e) {
					// TODO Auto-generated catch block
				System.out.println(e.getMessage());				
				}catch(NullPointerException ne) {
					System.out.println(ne.getMessage());
				
				
				}catch(Exception e) {
					e.printStackTrace();
				}
				//resp = pki.encrypt(,resp); falta buscar a chave privada do user
		    }
		

			
		}
		
	
		 	
	
	
	


}
