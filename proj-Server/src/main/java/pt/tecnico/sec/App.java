package pt.tecnico.sec;

/**
 * Aplicacao principal, lanca o notario e o Public Key Infraestructure
 *
 */
public class App 
{
	private static Notary notary;
	private static PKI pki;
	
    public static void main( String[] args )
    {
    	notary= new Notary();//atribuir aqui a porta
    	pki= new PKI();
    	
    }
}
