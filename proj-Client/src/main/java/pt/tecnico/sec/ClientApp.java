package pt.tecnico.sec;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class ClientApp 
{
	private static User user;
	private static final String IP = "127.0.0.1";
	private static final int PORT= 8081;
	private static ILibrary lib;

    public static void main( String[] args )
    {
        System.out.println( "Initiallizing user" );
        user = new User(IP, PORT);//recebe o ip e a porta 
        //cliente liga ao notario
        String op = System.console().readLine();
        readOperation (op);
        
        
    }
    
    public static void readOperation(String operation) {
    	
    }
}
