package pt.tecnico.sec;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import pt.tecnico.sec.*;

public class Notary {
	
	private String idNotary = "id1" ;
	private HashMap<Good, String> goods = new HashMap<Good, String>(); // <goodID,userID>

	
	
	
	/**
	 * Ativar o servidor
	 
	public static void main(String[] args) {
		
	}*/
	
	/**
	 * Verificar o pedido de venda do user
	 */
	private boolean verifySelling(String userID, String goodID) {
		if(goods.get(goodID).equals(userID))
			return true;
		return false;
	}
	
	/**
	 * Verificar o estado de um good e retornar ao user
	 * @param goodID
	 * @param userID
	 */
	private boolean verifiyStateOfGood(String goodID, String userID) {
		
		return false;
	}
	
	/**
	 * Retornar o estado do good
	 * 
	 * @param goodID
	 * @param userID
	 * @return Tuple com o id do good e o seu estado
	 */
//	private Object[] sendState( String goodID){
//		if(goods.containsKey(goodID)) {
//			Object[] pair = {goods.get(goodID), goods.get(goodID).getState()};
//			return pair;
//
//		}
//		return null;
//	}
//	
	/**
	 * Transferir o good ao user
	 * 
	 * @param goodID
	 * @return Transaction(?
	 *)
	 */
	private void transferGood( String goodID) {
		
	}
	
	public void startState() {
		Scanner scnr = null;
		try {
			
		String user = "";
		/* este é o caminho mais pequeno que conseguimos pôr a funcionar -Mário */
		File text = new File(".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt");
		
		scnr = new Scanner(text);
		while(scnr.hasNextLine()) {
			String line = scnr.nextLine();
			if(line.startsWith("#")) {
				user = new String();
				user = line.substring(1);
			}else{
				Good good = new Good(line);
				goods.put(good, user);
				
			}
			
		}
		
		}catch(Exception e) {
			System.out.println("Error in reading state file: " + e.getMessage());
		}finally {
			scnr.close();
		}
		System.out.println(goods);
	}
}
