package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;


public class User {
	private String idUser ;
	
	private HashMap<String,GoodState> Goods = new HashMap<String,GoodState>();
	private ILibrary lib;
	private static int ip;
	private static int port;
	private static final String OK ="Ok";
	private static final String NOK ="Not OK";
	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt";
	
	/**
	 * Conectar um cliente ao servidor (notary)
	
	public static void main(String[] args) {
		
	} */
	
	/**
	 * Informar ao notary que quer vender um good
	 */
	
	public User(String id, String ip, int port) {
		lib = new Library(ip, port);
		idUser = id;//hardcoded dps altero isto
		this.start();
		
	}
	
	public void printGoods() {
		System.out.println(Goods);
	}
	
	
	
	private void start() {
		File text = new File(path);
		String lines ="";

		Scanner scnr;
		try {
			scnr = new Scanner(text);
			while(scnr.hasNextLine()) {
				lines += " "+scnr.nextLine();	
			}
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String [] res = lines.split(" ");
		
		for (int i =0; i< res.length; i++) {
			if(res[i].startsWith("#")&& res[i].substring(1).equals(this.idUser)) {
				int j = i+1;
				while(!res[j].startsWith("#")) {
					Goods.put(res[j], GoodState.NOTONSALE);
					j++;
				}
				break;
			}
		}
		System.out.println(Goods);

	}
	
    public void readOperation(String operation) throws Exception {

    	String [] res = operation.split(" ");
    	if(res.length<2)
    		throw new Exception("Operation not valid: misgging arguments");
    	String op =  res[0];

    	if(op .equals("sell")) {

    		this.intentionToSell(res[1]);
    	}
    	if(op.equals("state"))
    		this.getStateOfGood(res[1]);
    	
    	if(op.equals("buy"))
    		this.buyGood(res[1]);
    	
    	if(op.equals("transfer")) {
    		if(res.length<3)
        		throw new Exception("Operation not valid: misgging arguments");
    		this.transferGood(res[1], res[2]);

    	}
    	
    	
    	
    }
    
	private void intentionToSell(String good) {
		String res =lib.intentionToSell(idUser, good);
		System.out.println(res);
		if( res.equals(OK))
			Goods.replace(good, GoodState.ONSALE);
		System.out.println(Goods);
		
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
	private String transferGood(String buyer, String good) {
		
		String res= lib.transferGood(idUser, buyer, good);
		
		if(res.equals(OK)) {
			Goods.remove(good);
			printGoods();
		}
		
		return res;
		
	}
	
	/**
	 * Transferir a informacao da compra
	 */
	private void transferBuyInfo() {
		
	}
}
