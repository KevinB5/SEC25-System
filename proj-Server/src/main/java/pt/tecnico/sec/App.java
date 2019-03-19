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
    	//notario liga o listener
    	pki= new PKI();
    	//notario aceita o cliente
    	//pki gera as chaves
    	//pki atribui a chave ao user que ligou
    	//notario recebe pedidos do cliente
    }
}
