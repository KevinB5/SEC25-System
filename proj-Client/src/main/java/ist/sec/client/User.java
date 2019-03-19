package hdsNotary;

import java.util.HashMap;


public class User {
	private String idUser ;
	private HashMap<String,GoodState> Goods = new HashMap<String,GoodState>();
	
	/**
	 * Conectar um cliente ao servidor (notary)
	 */
	public static void main(String[] args) {
		
	}
	
	/**
	 * Informar ao notary que quer vender um good
	 */
	private void intentionToSell(String good) {
		
	}
	
	/**
	 * Perguntar ao notary o estado de um good
	 * 
	 * @param good
	 * @return estado do good
	 */
	private GoodState getStateOfGood(String good) {
		return GoodState.NOTONSALE;
	}
	
	/**
	 * Informar ao notary que quer comprar um dado good
	 * 
	 * @param good
	 */
	private void buyGood (String good) {
		
	}
	
	/**
	 * Transferir um dado good
	 * 
	 * @param good
	 */
	private void transferGood(String good) {
		
	}
	
	/**
	 * Transferir a informação da compra
	 */
	private void transferBuyInfo() {
		
	}
}
