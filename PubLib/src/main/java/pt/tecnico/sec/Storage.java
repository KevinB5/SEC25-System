package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;


public class Storage {
	private static final String path = ".\\src\\main\\java\\pt\\tecnico\\sec\\goods.txt";
	private static final String path2 = ".\\src\\main\\java\\pt\\tecnico\\sec\\ports.txt";
	private static HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	
	private static final String[] users = {"user1", "user2"};
	
	public Storage() {
		start();
		System.out.println(Paths.get(path).getParent());

	}
	
	private void start() {
		//put user1
		String res = "good";
		
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
		}

		
		/*
		Scanner scnr = null;
		try {
				
			String user = "";
			/* este é o caminho mais pequeno que conseguimos pôr a funcionar -Mário 
			File text = new File(path);
		      		
			scnr = new Scanner(text);
			while(scnr.hasNextLine()) {
				String line = scnr.nextLine();
				if(line.startsWith("#")) {
					user = new String();
					user = line.substring(1);
				}else{
					goods.put(line, user);		
				}
			}
			System.out.println("Storage" + goods);
		}catch(Exception e) {
			System.out.println("Error in reading state file: " + e.getMessage());
		}finally {
			scnr.close();
		}*/
	}
	
	public HashMap getGoods() {
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

}
