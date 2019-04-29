package pt.tecnico.sec;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
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
	private static final int PORT = 8080;
	private static final String IP = "127.0.0.1";
	//Byzantine
	private static int f;
	private static int n=4;
	private static int rid;
	//keeps a list with the list of acks with the last ack, linked because it's a fifo
	private static List<String> ackList = new ArrayList<>();
	//keeps a list with the number reads with the last read value returned, linked because it's a fifo
	private static List<String[]> readList = new ArrayList<>();
	private static int nu;

    private static ObjectOutputStream[] sout= new ObjectOutputStream[n];
    private static ObjectInputStream[] sin=new ObjectInputStream[n];

	private static ArrayList<Socket> servConnects = new ArrayList<Socket>();
	// <ts,val> ???
	private static ArrayList<Notary> servers = new ArrayList<Notary>();
    
	
	
	public static void main( String[] args ) throws GeneralSecurityException, IOException
    {
		
    	System.out.println("What is the number of faulty processes?");
        String nu;
        nu= System.console().readLine();
        f = Integer.parseInt(nu);
        int N = 3*f+1; //expression to calculate total number of processes needed - might not be this
        /*
        System.out.println("Are we using the Citizen Card? (Y/N)");
        nu = System.console().readLine(); //nu will be Y or N which we use to obtain keys for notaries 
        
        System.out.println("Server ID");
        nu = Integer.parseInt(System.console().readLine());  
        */
        Storage store = new Storage();
        store.readLog();
        
    	//notary= new Notary(nu,store);//atribuir aqui a porta
        while(N!=0) {
        	notary= new Notary(N,store);//atribuir aqui a porta
        	servers.add(notary);
        	N--;
        }
        
        for(Notary n : servers) {
        	int id = Integer.parseInt(n.getID().substring(0, 1));
	    	new Thread(new Connector(id, notary)).start();

        }
        
        
    	/*
        try {
			serverSocket = new ServerSocket(PORT);
	        System.out.println("Server accepting connections on port: "+ (PORT+nu));

	        	String op = System.console().readLine();
	        	if(op.equals("connect")) {
	        		connectServers();
	        }
	        
	        while (true) {
	        	//TODO: Isto apenas recebe mensagens dos Clients?
	        	Socket clientSocket = serverSocket.accept();
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }
  

	private static void connectServers() {
		for(int x=1;x<=n;x++) {
			if(x!=nu) {
				try {
					Thread.sleep(500);
					
					Socket servConnect = new Socket(IP, PORT+x);
					System.out.println("connected to notary"+x+" at port: "+ (PORT+x));
					sout[x] = new ObjectOutputStream(servConnect.getOutputStream()); 
					sin[x] = new ObjectInputStream(servConnect.getInputStream());
					servConnects.add(servConnect);
				}catch (IOException | InterruptedException ex) {
					ex.printStackTrace();
				}
				
			}
		}*/
	}
    
	public final static class Connector implements Runnable {

    	public static final int PORT_NUMBER = 8081;
    	private Notary notary;
    	private ServerSocket serverSocket;
		private Socket clientSocket;


        public Connector(int port, Notary notary) {
        	System.out.println("Starting server in port "+ port);
			try {
				serverSocket = new ServerSocket(PORT_NUMBER +port);
	        	this.notary = notary;

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
	    			new Thread(new ClientReceiver(clientSocket, notary)).run();

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
		ClientReceiver(Socket client, Notary notary){
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
