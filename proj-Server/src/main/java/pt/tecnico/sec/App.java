package pt.tecnico.sec;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Aplicacao principal, lanca o notario e o Public Key Infraestructure
 *
 */
public class App 
{
	private static Notary notary;
	private static ServerSocket serverSocket;
	private static final int PORT = 8000;

	//Byzantine
	private static int f;
	private static int N;
	private static int J;
	private static int rid;
	//keeps a lisst with the list of acks with the last ack, linked because it's a fifo
	private static List<String> ackList = new ArrayList<>();
	//keeps a list with the number reads with the last read value returned, linked because it's a fifo
	private static List<String[]> readList = new ArrayList<>();
	// <ts,val> ???
	private static HashMap<Integer, Notary> servers = new HashMap<Integer, Notary>();
	private static HashMap<String, Integer> servPorts = new HashMap<String ,Integer>();//


    
	
	
	public static void main( String[] args ) throws GeneralSecurityException, IOException
    {
		
    	System.out.println("What is the number of faulty processes?");
        String nu;
        nu= System.console().readLine();
        f = Integer.parseInt(nu);
        N = 3*f+1; //expression to calculate total number of processes needed - might not be this
        /*
        System.out.println("Are we using the Citizen Card? (Y/N)");
        nu = System.console().readLine(); //nu will be Y or N which we use to obtain keys for notaries 

        System.out.println("Server ID");
        nu = Integer.parseInt(System.console().readLine());  
        */
        Storage store = new Storage();
        store.readLog();
        
        J=N;
    	//notary= new Notary(nu,store);//atribuir aqui a porta
        while(J!=0) {
        	notary= new Notary(J,store);//atribuir aqui a porta
        	servers.put(J, notary);
        	J--;
        }

        //para cada notario 
        for(Notary n : servers.values()) {
        	int id = Integer.parseInt(n.getID().substring(6));
        	servPorts.put(n.getID(), PORT+id);
        	//lança as threads que vao tratar dos sockets
	    	new Thread(new Connector(PORT+id, notary)).start();

        }
        
        store.writeServ(new ArrayList<>(servers.keySet()));
        
        //depois de todos os notarios serem lançados connectam se uns aos outros
        for(Notary n : servers.values()) {
        	System.out.println("Notary "+ n.getID() +" connecting to others");
	    	for(String Sid: servPorts.keySet()) {
	    		n.connect(Sid, servPorts.get(Sid));
	    	}
        }

        
              
	}
    
	public final static class Connector implements Runnable {


    	public static final int PORT_NUMBER = 8000;
    	private Notary notary;
    	private ServerSocket serverSocket;
		private Socket clientSocket;
		private int port;

		


        public Connector(int port, Notary notary) {
        	System.out.println("Starting server in port "+ port);
        	this.notary = notary;
	    	//notary.setServers(servSockt);


			try {
	    		System.out.println("OK");

				serverSocket = new ServerSocket(port);
		    	

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        	//this.serverSocket = serv;
           
        }
        
    	@Override
    	public void run() {

    		while(true) {
    			try {
					clientSocket = serverSocket.accept();

					
	    			new Thread(new ClientReceiver(clientSocket, notary,port)).start();
	    	

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
    	
	}
	
	private static class ClientReceiver implements Runnable {
		private Socket clientSocket;
		private Notary notary;
		ClientReceiver(Socket client, Notary notary, int port){
			clientSocket = client;
			this.notary = notary;
		}

    	@Override
    	public void run() {
    		 ObjectInputStream in = null;
    	        ObjectOutputStream out = null;
    	        
                ReadWriteLock lock = new ReentrantReadWriteLock();
                
                
    	        
    	        try {
    	            out = new ObjectOutputStream(clientSocket.getOutputStream()); 
    	            in = new ObjectInputStream(clientSocket.getInputStream());
    	        	while (true) {
    	        		Message msg =null;
    	        		//tenta adquirir o trinco para leitura
    	        		lock.readLock().lock();
    	        		try {
    	        			msg= (Message) in.readObject();
    	        			System.out.println("got a message! ");
    	        		}finally {
    	        			//liberta o trinco assim q termina
    	        			lock.readLock().unlock();
    	        		}
    			        String cmd = msg.getText();
    			        String[] spl = cmd.split(" ");
    			        
						Message res = notary.execute(msg);

    			        
    			        //tenta adquirir o trinco para escrita
    			        lock.writeLock().lock();
    				        try {
    							out.writeObject(res);
    						} catch (Exception e) {
    							e.printStackTrace();
    						}finally {
    							//liberta assim que terminar a escrita
    							lock.writeLock().unlock();
    						}
    			        }

    	        } catch (IOException | ClassNotFoundException ex) {
    	            System.out.println("Unable to get streams from client");
    	        } catch (Exception e) {
    				e.printStackTrace();
    			} finally {
    	            try {
    	                in.close();
    	                out.close();
    	                clientSocket.close();
    	            } catch (IOException ex) {
    	                ex.printStackTrace();
    	            }
    	        }		
    	}
    }
}
