package pt.tecnico.sec;

/**
 * Hello world!
 *
 */
public class ClientApp 
{
	private static User user;
	private static final String IP = "127.0.0.1";
	private static final int PORT= 8081;
    public static void main( String[] args )
    {
        System.out.println( "Initiallizing user" );
        user = new User(IP, PORT);//recebe o ip e a porta 
        //cliente liga ao notario
        
    }
}
