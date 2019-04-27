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
	static Notary notary ;
	private static ArrayList<ServerSocket> serverSockets = new ArrayList<ServerSocket>();
	private static final int PORT = 8081;
	//Byzantine
	private static int f;
	private static int N;
	private static int rid;
	//keeps a list with the list of acks with the last ack, linked because it's a fifo
	private static List<String> ackList = new ArrayList<>();
	//keeps a list with the number reads with the last read value returned, linked because it's a fifo
	private static List<String[]> readList = new ArrayList<>();
	// <ts,val> ???
    
	public static void main( String[] args ) throws GeneralSecurityException, IOException
    {
    	System.out.println("What is the number of faulty processes?");
        String nu;
        nu= System.console().readLine();
        f = Integer.parseInt(nu);
        N = 2*f+1; //expression to calculate total number of processes needed - might not be this
        
        System.out.println("Are we using the Citizen Card? (Y/N)");
        nu = System.console().readLine(); //nu will be Y or N which we use to obtain keys for notaries 
        
        
        for (int n=0;n<N;n++) {
	    	notary = new Notary();//atribuir aqui a porta
	        try {
				ServerSocket serverSocket = new ServerSocket(PORT+n);
		        System.out.println("Server accepting connections on port: "+ PORT);
		        serverSockets.add(serverSocket);
		        while (true) {
		        	Socket clientSocket = serverSocket.accept();
		        	(new Thread(new ConnectClient(clientSocket, notary))).start();
		        }
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
  
    public final static class ConnectClient  implements Runnable {

    	public static final int PORT_NUMBER = 8081;
    	private Notary notary;
    	private Socket clientSocket;

        public ConnectClient(Socket client, Notary notary) {
        	this.notary = notary;
        	this.clientSocket = client;
           
        }

    	@Override
    	public void run() {
    		 ObjectInputStream in = null;
    	        ObjectOutputStream out = null;
    	        
                ReadWriteLock lock = new ReentrantReadWriteLock();
    	        try {
    	            out = new ObjectOutputStream(clientSocket.getOutputStream()); 
    	            in = new ObjectInputStream(clientSocket.getInputStream());
    	            
    	            lock.writeLock().lock();
			        try {
						out.writeObject(new Message("0",""+f,null,null,null,null));
					} catch (Exception e) {
						e.printStackTrace();
					}finally {
						//liberta assim que terminar a escrita
						lock.writeLock().unlock();
					}
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
