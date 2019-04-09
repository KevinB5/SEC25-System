package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;


public class Storage {
	//private static String path = ".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt";
	//private static String path = "../../"
	private static String line = System.getProperty("file.separator");
	private static String filename = line +"goods.txt";
	private static String path = originPath() + filename;
	private static HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	
	
	private static final String[] users = {"user1", "user2"};
	
	private InputStream text;
	private File systemFile;
	
	public Storage() {
		//start();
		//path = Storage.class.getResource("Storage.class").getPath();
		text = (this.getClass().getClassLoader().getResourceAsStream("goods.txt"));
		start();
		//System.out.println(path.getParent());

	}
	
	public static String originPath() {
		String origin = System.getProperty("user.dir");
		System.out.println(origin);
		int lastBar = 0;
		for(int i=0; i < origin.length() ; i++) {
			if(origin.charAt(i)==line.charAt(0))
				lastBar=i;
		}
		System.out.println(origin.substring(0,lastBar));
		return origin.substring(0,lastBar);
	}
	
	private void start() {
		//put user1
		String res = "good";
		/*
		
		for(int i = 1;i<7 ;i++) {
			if(i>3) {
				goods.put( res+i, users[0]);
				}
			else {

				goods.put( res+i, users[1]);
			}
		}
		
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("goods.txt", "UTF-8");
			

		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		System.out.println(path);
		systemFile = new File(path);

		
		Scanner scnr = null;
		PrintWriter pwriter = null;
		try {
			//System.out.println(systemFile.isFile());
				
			String user = "";
			//este é o caminho mais pequeno que conseguimos pôr a funcionar -Mário 
			//File text = new File(path);
		    //pwriter = new PrintWriter(new FileWriter(systemFile));

			scnr = new Scanner(systemFile);
			while(scnr.hasNextLine()) {
				String line = scnr.nextLine();
				if(line.startsWith("#")) {
					user = new String();
					user = line.substring(1);
				}else{
					goods.put(line, user);		
				}
				//pwriter.println(line);
			}
		}catch(Exception e) {
			System.out.println("Error in reading state file: " + e.getMessage());
		}finally {
			scnr.close();
		}
		
	
	}
	
	public HashMap<String, String> getGoods() {
		return goods;//goodID, userID
	}
	
	
	
	public ArrayList<String> getGoods(String userID) {
		ArrayList<String> res = new ArrayList<String>();
		for(String goodID: goods.keySet() ) {
			if(userID.equals(goods.get(goodID)))
				res.add(goodID);
				}
		return res;
	}
	
	public void upDateFile(String goodID, String newOwner) {
		try {


		      if (!systemFile.isFile()) {
		        System.out.println("Parameter is not an existing file");
		        return;
		      }
		      


		      //Construct the new file that will later be renamed to the original filename.
		      File tempFile = new File(systemFile.getAbsolutePath() + ".tmp");

		      BufferedReader reader = new BufferedReader(new FileReader(systemFile.getAbsolutePath() ));
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
		      if (!systemFile.delete()) {
		        System.out.println("Could not delete file");
		        return;
		      }

		      //Rename the new file to the filename the original file had.
		      if (!tempFile.renameTo(systemFile))
		        System.out.println("Could not rename file");
		      
		     

		    }
		    catch (FileNotFoundException ex) {
		      ex.printStackTrace();
		    }
		    catch (IOException ex) {
		      ex.printStackTrace();
		    }
		goods.replace(goodID, newOwner);
	}

	
}
