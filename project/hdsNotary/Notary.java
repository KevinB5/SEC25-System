package hdsNotary;

import java.util.HashMap;

import javafx.util.Pair;

public class Notary {
	private String idNotary = "id1" ;
	private HashMap<String, String> Goods = new HashMap<String, String>(); // <goodID,userID>
	
	
	/**
	 * Ativar o servidor
	 */
	public static void main(String[] args) {
		
	}
	
	/**
	 * Verificar o pedido de venda do user
	 */
	private void verifySelling() {
		
	}
	
	/**
	 * Verificar o estado de um good e retornar ao user
	 * @param GoodID
	 * @param userID
	 */
	private boolean verifiyStateOfGood(String GoodID, String userID) {
		return false;
	}
	
	/**
	 * Retornar o estado do good
	 * 
	 * @param GoodID
	 * @param userID
	 * @return Tuple com o id do good e o seu estado
	 */
	private Pair<String, GoodState> sendState( String GoodID, String userID){
		Pair<String,GoodState> pair = new Pair<>(GoodID,GoodState.NOTONSALE);
		return pair;
	}
	
	/**
	 * Transferir o good ao user
	 * 
	 * @param GoodID
	 */
	private void transferGood( String GoodID) {
		
	}
}
