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
	private static final String ID ="user";
	private static ILibrary lib;
	

    public static void main( String[] args )
    {
        System.out.println( "Initiallizing user" );
        user = new User(ID + 1, IP, PORT);//recebe o ip e a porta 
        
        //cliente liga ao notario
        System.out.println("User initialized");
        while(true) {
            String op = System.console().readLine();
            try {
     		user.readOperation (op);
     	} catch (Exception e) {
     		// TODO Auto-generated catch block
     		//e.printStackTrace();
     		System.out.println("missing arguments");
     	}
        }

        
    }
    

}
