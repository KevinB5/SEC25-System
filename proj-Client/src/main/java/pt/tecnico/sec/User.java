package pt.tecnico.sec;

import java.util.HashMap;


public class User {
	private String idUser ;
	
	private HashMap<String,GoodState> Goods = new HashMap<String,GoodState>();
	private ILibrary lib;
	private static int ip;
	private static int port;
	
	/**
	 * Conectar um cliente ao servidor (notary)
	
	public static void main(String[] args) {
		
	} */
	
	/**
	 * Informar ao notary que quer vender um good
	 */
	
	public User(String ip, int port) {
		lib = new Library(ip, port);
		
	}
	private void intentionToSell(String good) {
		lib.intentionToSell(idUser, good);
	}
	
	/**
	 * Perguntar ao notary o estado de um good
	 * 
	 * @param good
	 * @return estado do good
	 */
	private String getStateOfGood(String good) {
		return lib.getStateOfGood(good);
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
	 * Transferir a informacao da compra
	 */
	private void transferBuyInfo() {
		
	}
}
