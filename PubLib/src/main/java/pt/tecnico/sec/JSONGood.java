package pt.tecnico.sec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONGood {
	
	private final String PATH = "goods";
	

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
				JSONArray newUserList = new JSONArray();
				
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
	
}
