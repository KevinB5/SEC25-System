package pt.tecnico.sec;

import java.io.File;
import java.util.HashMap;
import java.util.Scanner;

import pt.tecnico.sec.*;

public class Notary {
	
	private String idNotary = "id1" ;
	private static final String OK ="Ok";
	private static final String NOK ="Not OK";
	private HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	private HashMap<String, GoodState> states = new HashMap<String, GoodState>(); // <goodID,userID>

	
	private enum GoodState {
		ONSALE,NOTONSALE
	}
	
	/**
	 * Ativar o servidor
	 
	public static void main(String[] args) {
		
	}*/
	
	/**
	 * Verificar o pedido de venda do user 
	 * @throws Exception 
	 */
	private String verifySelling(String userID, String goodID) throws Exception {
		if(!goods.containsKey(goodID))
			return "No such good";
		if(goods.get(goodID).equals(userID)) {
			states.replace(goodID, GoodState.ONSALE);
			System.out.println(states);
			
			return OK;
		}
		return NOK;
	}
	
	/**
	 * Verificar o estado de um good e retornar ao user
	 * @param goodID
	 * @param userID
	 */
	private String verifiyStateOfGood(String goodID) {
		if(!goods.containsKey(goodID))
			return "No such good";
		String state = "<";
		state += goods.get(goodID) + " , " + states.get(goodID).toString()+">";
		return state;
	}
	
	/**
	 * Retornar o estado do good
	 * 
	 * @param goodID
	 * @param userID
	 * @return Tuple com o id do good e o seu estado
	 *
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
	 * Ler os comandos do utilizador e realizar as operacoes respetivas
	 * @param command
	 * @throws Exception
	 */
	
	public String execute(String command) throws Exception {

    	String [] res = command.split(" ");
    	if(res.length<2)
    		throw new Exception("Operation not valid: misgging arguments");
    	String op =  res[0];

    	if(op .equals("sell")) {

    		return this.verifySelling(res[1], res[2]);//userID, goodID
    	}
    	if(op.equals("state"))
    		return this.verifiyStateOfGood(res[1]);//goodID
    	/*
    	if(op.equals("buy"))
    		this.buyGood(res[1]);*/
    	
    	if(op.equals("transfer"))
    		return this.transferGood(res[1],res[2],res[3]);//buyer, seller, goodID
    	else
    		return "no valid operation";
	}
	/**
	 * Transferir o good ao user
	 * 
	 * @param goodID
	 * @return Transaction(?
	 *)
	 */
	private String transferGood( String seller,String buyer , String goodID) {
		if(goods.get(goodID).equals(seller)) {
			if(states.get(goodID).equals(GoodState.ONSALE)) {
				goods.replace(goodID, buyer);
				states.replace(goodID, GoodState.NOTONSALE);
				printGoods();
				return OK;
			}
			else
				return "Good not on sale";
		}
		return NOK;
	}
	
	public void printGoods() {
		System.out.println(goods);
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
				goods.put(line, user);	
				states.put(line,GoodState.NOTONSALE);
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
