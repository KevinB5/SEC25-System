package pt.tecnico.sec;

import java.util.HashMap;
import pt.tecnico.sec.*;

public class Notary {
	
	private String idNotary = "id1" ;
	private HashMap<String, Good> Goods = new HashMap<String, Good>(); // <goodID,userID>

	
	
	
	/**
	 * Ativar o servidor
	 
	public static void main(String[] args) {
		
	}*/
	
	/**
	 * Verificar o pedido de venda do user
	 */
	private boolean verifySelling(String userID, String goodID) {
		if(Goods.get(goodID).equals(userID))
			return true;
		return false;
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
	private Object[] sendState( String GoodID){
		if(Goods.containsKey(GoodID)) {
			Object[] pair = {Goods.get(GoodID), Goods.get(GoodID).getState()};
			return pair;

		}
		return null;
	}
	
	/**
	 * Transferir o good ao user
	 * 
	 * @param GoodID
	 * @return Transaction(?
	 *)
	 */
	private void transferGood( String GoodID) {
		
	}
}
