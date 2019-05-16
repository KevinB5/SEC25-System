package pt.tecnico.sec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONGood {
	
	private String origin;
	private  String line = System.getProperty("file.separator");
	private String PATH ;
	private String filename = "goods";

	/**
	 * ATENÇÃO O ESTADO DOS GOODS É "onsale" OU "notonsale"
	 */
	
	public JSONGood() {
			origin = System.getProperty("user.dir");
			int lastBar = 0;
			for(int i=0; i < origin.length() ; i++) {
				if(origin.charAt(i)==line.charAt(0))
					lastBar=i;
			}
			origin = origin.substring(0,lastBar);
			PATH = origin+line+filename;
			System.out.println(PATH);
    }

    
	//manipulatorID is the ID of the entity that is using this method (for example notary1)
	public void updateFile(String goodID, String newOwner,String goodState, String manipulatorID) {
		File myFile=null;
		try {
			myFile = new File(PATH+manipulatorID+".json");
			myFile.createNewFile();
			    
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONArray newUserList = new JSONArray();
		
		if(myFile.length()>0) {
			JSONParser jsonParser = new JSONParser();
			try ( FileReader reader = new FileReader(PATH+manipulatorID+".json")){
			
			
				Object obj = jsonParser.parse(reader);
				
				JSONArray userList = (JSONArray) obj;
				
				for( Object userObject : userList) {
					JSONObject user = (JSONObject)userObject;
					JSONObject goods = (JSONObject)user.get("goods");
					
					if(!user.get("userID").equals(newOwner) && goods.get(goodID)!=null) {
						goods.remove(goodID);
						user.put("goods",goods);
					}else if(user.get("userID").equals(newOwner)){
						goods.put(goodID,goodState);
						user.put("goods",goods);
					}
					newUserList.add(user);
					
				}
				
			try( FileWriter writer = new FileWriter(PATH+manipulatorID+".json")){
				writer.write(newUserList.toJSONString());
				writer.flush();
			}
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}else {
			JSONObject user = new JSONObject();
			JSONObject goods = new JSONObject();
			goods.put(goodID,goodState);
			user.put("goods",goods);
			user.put("userID",newOwner);
			newUserList.add(user);
			
			try( FileWriter writer = new FileWriter(PATH+manipulatorID+".json")){
				writer.write(newUserList.toJSONString());
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//manipulatorID is the ID of the entity that is using this method (for example notary1)
	public String findUser( String goodID,String manipulatorID) {
		try {
			File myFile = new File(PATH+manipulatorID+".json");
			if(myFile.createNewFile())
				return null;
			
			JSONParser jsonParser = new JSONParser();
			try ( FileReader reader = new FileReader(PATH+manipulatorID+".json")){
				Object obj = jsonParser.parse(reader);
				
				JSONArray userList = (JSONArray) obj;
				
				for( Object userObject : userList) {
					JSONObject user = (JSONObject)userObject;
					JSONObject goods = (JSONObject)user.get("goods");
					if(goods.get(goodID)!=null)
						return user.get("userID").toString();
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			    
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	//manipulatorID is the ID of the entity that is using this method (for example notary1)
		public String verifySelling(String sellerID, String goodID,String manipulatorID) {
			try {
				File myFile = new File(PATH+manipulatorID+".json");
				if(myFile.createNewFile())
					return "Not OK";
				
				JSONParser jsonParser = new JSONParser();
				try ( FileReader reader = new FileReader(PATH+manipulatorID+".json")){
					Object obj = jsonParser.parse(reader);
					
					JSONArray userList = (JSONArray) obj;
					
					for( Object userObject : userList) {
						JSONObject user = (JSONObject)userObject;
						JSONObject goods = (JSONObject)user.get("goods");
						if(user.get("userID").equals(sellerID))
							return "ACK";
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				    
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return "Not OK";
		}


		public List<String> getGoodList(String manipulatorID) {
			List<String> goodList = new ArrayList<String>();
			
			try {
				File myFile = new File(PATH+manipulatorID+".json");
				if(myFile.createNewFile())
					return goodList;
				
				JSONParser jsonParser = new JSONParser();
				try ( FileReader reader = new FileReader(PATH+manipulatorID+".json")){
					Object obj = jsonParser.parse(reader);
					
					JSONArray userList = (JSONArray) obj;
					
					for( Object userObject : userList) {
						JSONObject user = (JSONObject)userObject;
						JSONObject goods = (JSONObject)user.get("goods");
						for(Object good : goods.keySet()) {
							goodList.add((String) good);
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				    
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(goodList.size()==0)
				return getGoodList("");
			return goodList;
		}
		
		public List<String> getMyGoodList(String manipulatorID) {
			List<String> goodList = new ArrayList<String>();
			
			try {
				File myFile = new File(PATH+manipulatorID+".json");
				if(myFile.createNewFile())
					return goodList;
				
				JSONParser jsonParser = new JSONParser();
				try ( FileReader reader = new FileReader(PATH+manipulatorID+".json")){
					Object obj = jsonParser.parse(reader);
					
					JSONArray userList = (JSONArray) obj;
					
					for( Object userObject : userList) {
						JSONObject user = (JSONObject)userObject;
						JSONObject goods = (JSONObject)user.get("goods");
						String userID = (String)user.get("userID");
						for(Object good : goods.keySet() ) {
							if(userID.equals("user"+manipulatorID)) 
								goodList.add((String) good);
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				    
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(goodList.size()==0)
				return getGoodList("");
			return goodList;
		}
		
		public boolean existGood(String goodID,String manipulatorID) {
			try {
				File myFile = new File(PATH+manipulatorID+".json");
				if(myFile.createNewFile())
					return false;
				
				JSONParser jsonParser = new JSONParser();
				try ( FileReader reader = new FileReader(PATH+manipulatorID+".json")){
					Object obj = jsonParser.parse(reader);
					
					JSONArray userList = (JSONArray) obj;
					
					for( Object userObject : userList) {
						JSONObject user = (JSONObject)userObject;
						JSONObject goods = (JSONObject)user.get("goods");
						for(Object good : goods.keySet()) {
							String compareGood = (String)good;
							if(compareGood.equals(goodID))
								return true;
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				    
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
		}
		
		
		public String getGoodState(String goodID,String manipulatorID) {
			try {
				File myFile = new File(PATH+manipulatorID+".json");
				if(myFile.createNewFile())
					return "notonsale";
				
				JSONParser jsonParser = new JSONParser();
				try ( FileReader reader = new FileReader(PATH+manipulatorID+".json")){
					Object obj = jsonParser.parse(reader);
					
					JSONArray userList = (JSONArray) obj;
					
					for( Object userObject : userList) {
						JSONObject user = (JSONObject)userObject;
						JSONObject goods = (JSONObject)user.get("goods");
						for(Object good : goods.keySet()) {
							String compareGood = (String)good;
							if(compareGood.equals(goodID))
								return (String)goods.get(good);
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				    
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return "notonsale";
		}
		
		public String getGoodUser(String goodID,String manipulatorID) {
			try {
				File myFile = new File(PATH+manipulatorID+".json");
				if(myFile.createNewFile())
					return null;
				
				JSONParser jsonParser = new JSONParser();
				try ( FileReader reader = new FileReader(PATH+manipulatorID+".json")){
					Object obj = jsonParser.parse(reader);
					
					JSONArray userList = (JSONArray) obj;
					
					for( Object userObject : userList) {
						JSONObject user = (JSONObject)userObject;
						JSONObject goods = (JSONObject)user.get("goods");
						for(Object good : goods.keySet()) {
							String compareGood = (String)good;
							if(compareGood.equals(goodID))
								return (String)user.get("userID");
						}
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				    
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
}
