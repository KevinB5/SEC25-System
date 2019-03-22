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

import pt.tecnico.sec.*;

public class Notary {
	
	private String idNotary = "id1" ;
	private static final String OK ="Ok";
	private static final String NOK ="Not OK";
	private HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	private HashMap<String, GoodState> states = new HashMap<String, GoodState>(); // <goodID,userID>
	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt";
	
	public Notary() {
		
	}

	
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
				this.upDateFile(goodID, buyer);
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
	
	private void upDateFile(String goodID, String newOwner) {
		try {

		      File inFile = new File(path);

		      if (!inFile.isFile()) {
		        System.out.println("Parameter is not an existing file");
		        return;
		      }

		      //Construct the new file that will later be renamed to the original filename.
		      File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

		      BufferedReader reader = new BufferedReader(new FileReader(path));
		      PrintWriter pwriter = new PrintWriter(new FileWriter(tempFile));

		      String line = null;
		      boolean foundOwner =false;
		      boolean written = false;

		      //Read from the original file and write to the new
		      //unless content matches data to be removed.
		      while ((line = reader.readLine()) != null) {

		        if (!line.trim().equals(goodID)) {
		        	if(foundOwner&!written) {
		        		pwriter.println(goodID);
		        		written=true;
		        	}
		        	else if(line.trim().equals('#'+newOwner))
		        		foundOwner=true;
		          pwriter.println(line);
		          pwriter.flush();
		        }
		      }
		      pwriter.close();
		      reader.close();
		      
		      

		      //Delete the original file
		      if (!inFile.delete()) {
		        System.out.println("Could not delete file");
		        return;
		      }

		      //Rename the new file to the filename the original file had.
		      if (!tempFile.renameTo(inFile))
		        System.out.println("Could not rename file");

		    }
		    catch (FileNotFoundException ex) {
		      ex.printStackTrace();
		    }
		    catch (IOException ex) {
		      ex.printStackTrace();
		    }
		
	}
	
	public void startState() {
		Scanner scnr = null;
		try {
			
		String user = "";
		/* este é o caminho mais pequeno que conseguimos pôr a funcionar -Mário */
		File text = new File(path);
		
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
