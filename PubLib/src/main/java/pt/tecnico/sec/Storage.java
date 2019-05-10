package pt.tecnico.sec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;



public class Storage {
	//private static String path = ".\\src\\main\\java\\pt\\tecnico\\state\\goods.txt";
	//private static String path = "../../"
	private  String line = System.getProperty("file.separator");
	private  String filename = line +"goods";
	private  String Sfilename = line +"servPorts.txt";
	private  String path = originPath() + filename;
	private  String path2 = originPath() + Sfilename;

	private static HashMap<String, String> goods = new HashMap<String, String>(); // <goodID,userID>
	private static HashMap<String, String> states = new HashMap<String, String>(); // <goodID,userID>

	private final String pathLog= System.getProperty("user.dir")+"\\src\\main\\java\\pt\\tecnico\\state\\";
	private String logName = "";
		
	private HashMap<String,Integer> servs= new HashMap<String,Integer>() ;
	private File systemFile;
	private File servFile;
	private FileOutputStream out ;
		
	public String originPath() {
		String origin = System.getProperty("user.dir");
		int lastBar = 0;
		for(int i=0; i < origin.length() ; i++) {
			if(origin.charAt(i)==line.charAt(0))
				lastBar=i;
		}
		return origin.substring(0,lastBar);
	}
	
	public Storage(int id) {
		PKI.getInstance();
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
		this.path = path+id+".txt";
		systemFile = new File(path);
		if(!systemFile.isFile())
			try {
				systemFile.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


		
		Scanner scnr = null;
		PrintWriter pwriter = null;
		try {
			//System.out.println(systemFile.isFile());
				
			String user = "";
			String[] temp ;
			String goodID = "";
			String state = "";
			
			//File text = new File(path);
		    //pwriter = new PrintWriter(new FileWriter(systemFile));

			scnr = new Scanner(systemFile);
			while(scnr.hasNextLine()) {
				String line = scnr.nextLine();
				if(line.startsWith("#")) {
					user = line.substring(1);
				}else if(!line.isEmpty()){
					temp= line.split(" ");
					goodID = temp[0];
					state = temp[1];
					System.out.println("state: "+ state);
					goods.put(goodID, user);
					states.put(goodID, state);
				}
				//pwriter.println(line);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			scnr.close();
		}
		
	
	}
	
	public void writePorts(File servFile) {
		BufferedWriter bw = null;
		FileWriter fw = null;
		String data ="";
		

		for(String server : servs.keySet()) {
			data = "#" + server + " " +servs.get(server) + " ";
			try {
				
				fw = new FileWriter(servFile.getAbsoluteFile(), true);
				bw = new BufferedWriter(fw);
				bw.write(data);
				bw.write(System.getProperty("line.separator"));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bw != null)
						bw.close();
					if (fw != null)
						fw.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public HashMap<String, String> getGoods() {
		return goods;//goodID, userID
	}
	
	public HashMap<String, String> getStates() {
		return states;//goodID, userID
	}
	
	public void setLog(String name) {
		this.logName = "tf"+name+".txt";
	}
	
	public void writeServ(HashMap<String, Integer>ports) {
		this.servs = ports;
		servFile = new File(path2);
		try {
			if (!servFile.exists()) {
				
					servFile.createNewFile();
				
			}
			else {
				System.out.println("Deleting existing file");
				if(servFile.delete()) {
					servFile = new File(path2);
					servFile.createNewFile();
					}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.writePorts(servFile);
		
	}
	
	public HashMap<String, Integer>readServs(){
		
		servFile = new File(path2);
		
		Scanner scnr = null;
		PrintWriter pwriter = null;
		try {
			//System.out.println(systemFile.isFile());
				
			String notary = "";
			//File text = new File(path);
		    //pwriter = new PrintWriter(new FileWriter(systemFile));

			scnr = new Scanner(servFile);
			while(scnr.hasNextLine()) {
				String line = scnr.nextLine();
				String[] fg = line.split(" ");
				if(fg.length >1) {
					servs.put(fg[0].substring(1), Integer.parseInt(fg[1]));
				}
				//pwriter.println(line);
			}
		}catch(Exception e) {
			e.printStackTrace();
			//System.out.println("Error in reading state file: " + e.getMessage());
		}finally {
			scnr.close();
		}

		return servs;
	}
	
	
	
	public HashMap<String, String> getGoods(String userID) {
		HashMap<String, String> res = new HashMap<String, String>();

		for(String good: goods.keySet() ) {
			if(userID.equals(goods.get(good)))
				res.put(good, states.get(good));
				}
		return res;
	}
	
	
	
	public void updateFile(String goodID, String newOwner) {
		try {
			//systemFile = new File(path+id+".txt");


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

	public void writeLog(String goodId, String seller, String buyer,String counter , byte[] sigSeller,byte[] sigBuyer) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		String data = goodId+";"+seller+";"+buyer+";"+counter+";"+sigSeller+";"+sigBuyer+";$"+System.lineSeparator();
		try {
			System.out.println("heyyyy");
			File file = new File(this.pathLog+ this.logName);
			if (!file.exists()) {
				file.createNewFile();
			}
			fw = new FileWriter(file.getAbsoluteFile(), true);
			System.out.println("heyyyy222");

			bw = new BufferedWriter(fw);
			bw.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void readLog() {
		File systemFile = new File(pathLog+logName);
		if(!systemFile.isFile())
			try {
				systemFile.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		File tmpFile = new File(pathLog+"tmp.txt");
		Scanner scnr = null;
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			scnr = new Scanner(systemFile);
			while(scnr.hasNextLine()) {
				String line = scnr.nextLine();
				String[] content = line.split(";");
				if(!line.startsWith("#") && content.length==7) {
					if(content[content.length-1].equals("$")) {
						if (!tmpFile.exists()) {
							tmpFile.createNewFile();
						}
						fw = new FileWriter(tmpFile.getAbsoluteFile(), true);
						bw = new BufferedWriter(fw);
						bw.write(line);	
					}
				}
			}
		
		tmpFile.renameTo(systemFile);
		
//			System.out.println("Log " + log);
		}catch(Exception e) {
			System.out.println("Error in reading state file: " + e.getMessage());
		}finally {
			scnr.close();
		}
	}
}
