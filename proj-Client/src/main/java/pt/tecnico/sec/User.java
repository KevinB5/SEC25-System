package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;


public class User {
	private String idUser ;
	
	private HashMap<String,GoodState> goods = new HashMap<String,GoodState>();
	private HashMap<String,String> usrPorts = new HashMap<String,String>();
	private Library lib;
	private static final String OK ="Ok";
	private static final String NOK ="Not OK";
	private String ip;
	private static final String path = "goods.txt";
	private static final String path2 = ".\\src\\main\\java\\pt\\tecnico\\state\\ports.txt";

	private static ServerSocket serverSocket=null;
    private static int PORT;


	
	/**
	 * Conectar um cliente ao servidor (notary)
	
	public static void main(String[] args) {
		
	} */
	
	/**
	 * Informar ao notary que quer vender um good
	 */
	
	public User(String id, String ip, int Svport) {
		idUser = id;
		this.ip=ip;
		this.start();
		this.getPort();
		lib = new Library(id, ip, Svport, PORT);
		
	}
	
	public int gtPort() {
		return this.PORT;
	}
	
	public void printgoods() {
		System.out.println(goods);
	}
	
	public void connectToUsers() {
		for(String key : usrPorts.keySet()) {
			lib.connectUser(ip, Integer.parseInt(usrPorts.get(key)));
		}		
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
				System.out.println(res[i].substring(1));
				int j = i+1;
				while(j< res.length && !res[j].startsWith("#") ) {
					goods.put(res[j], GoodState.NOTONSALE);
					j++;
				}
				break;
			}
		}
		System.out.println(goods);
		
	}
	
	private void getPort() {
			File text = new File(path2);
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
			String uid ="";
			for (int i =0; i< res.length; i++) {


				if(i%2==0)
					uid=res[i];
				
				if(i%2!=0) {
					if(res[i].equals("#"+this.idUser)) {
						PORT = Integer.parseInt(res[i+1]);
						continue;
					}
					usrPorts.put(res[i].substring(1), res[i+1]);
				}
				
			}
		
	}
	
    public void readOperation(String operation) throws Exception {

    	String [] res = operation.split(" ");

    	String op =  res[0];

    	if(op .equals("sell")) {

    		this.intentionToSell(res[1]);
    	}
    	if(op.equals("state"))
    		this.getStateOfGood(res[1]);
    	
    	if(op.equals("buy"))
    		this.buyGood(res[1], res[2]);
    	
    	if(op.equals("transfer")) {
    		if(res.length<3)
        		throw new Exception("Operation not valid: misgging arguments");
    		this.transferGood(res[1], res[2]);

    	}
    	
    	if(op.equals("connect"))
    		this.connectToUsers();
    	
    	
    	
    }
    
	private void intentionToSell(String good) {
		String res =lib.intentionToSell(idUser, good);
		System.out.println(res);
		if( res.equals(OK))
			goods.replace(good, GoodState.ONSALE);
		System.out.println(goods);
		
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
	private void buyGood (String user, String good) {
		
		String res=  lib.buyGood(user, good);
		
		if(res.equals(OK)) {
			goods.put(good, GoodState.NOTONSALE);
			printgoods();
		}
	}
	
	/**
	 * Transferir um dado good
	 * 
	 * @param good
	 */
	private String transferGood(String buyer, String good) {
		
		String res= lib.transferGood(idUser, buyer, good);
		
		if(res.equals(OK)) {
			sellGood(good);
			goods.remove(good);
			printgoods();
		}
		
		return res;
		
	}
	
	private void sellGood(String goodID) {
		System.out.println("selling "+ goodID);
	}
	
	/**
	 * Transferir a informacao da compra
	 */
	private void transferBuyInfo() {
		
	}
	
	public String execute(String command) throws Exception {

    	String [] res = command.split(" ");
    	if(res.length<2)
    		throw new Exception("Operation not valid: misgging arguments");
    	String op =  res[0];
    	System.out.println("trying to buy "+ res[1]+res[2]);
    	if(op .equals("intentionbuy")) {//buy buyerID goodID
    		String ret = "no such good";
    		if(goods.containsKey(res[2])) {
    			ret= lib.sellGood(this.idUser, res[1], res[2]);//sellerID, goodID
	    		if(ret.equals("Ok"))
	    			goods.remove(res[2], goods.get(res[2]));
	    		this.printgoods();
	    		}
    		return ret;
    	}

    	else
    		return "no valid operation " + command ;
	}
}
