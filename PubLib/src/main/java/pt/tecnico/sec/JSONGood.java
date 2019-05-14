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
	
	public void updateFile(String goodID, String newOwner) {
		 
		try {
			File myFile = new File("goods.json");
			myFile.createNewFile();
			    
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		JSONParser jsonParser = new JSONParser();
		try ( FileReader reader = new FileReader("goods.json")){
			Object obj = jsonParser.parse(reader);
			
			JSONArray userList = (JSONArray) obj;
			JSONArray newUserList = new JSONArray();
			
			for( Object userObject : userList) {
				JSONObject user = (JSONObject)userObject;
				JSONObject good = new JSONObject();
				good.put(goodID, "n");
				
				System.out.println("user: "+user);
				System.out.println("value: "+user.get("userID"));
				System.out.println("value: "+user.get("goods"));
				
				if(user.get(goodID)!=null) {
					user.remove(goodID);
					System.out.println("object removed: "+user);
					
				}else {
					System.out.println("owner found");
					user.put(newOwner,good);
				}
				newUserList.add(user);
			}
//			JSONObject good = new JSONObject();
//			good.put(goodID, "n");
//			JSONObject user = new JSONObject();
//			user.put(newOwner, good);
//			newUserList.add(user);
			
//		try( FileWriter writer = new FileWriter("goods.json")){
//			writer.write(newUserList.toJSONString());
//			writer.flush();
//		}
			
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
		
		
	}
	
	public boolean getGood( String goodID,String userID) {
		try {
			File myFile = new File("goods.json");
			if(myFile.createNewFile())
				return false;
			
			JSONParser jsonParser = new JSONParser();
			try ( FileReader reader = new FileReader("goods.json")){
				Object obj = jsonParser.parse(reader);
				
				JSONArray userList = (JSONArray) obj;
				JSONArray newUserList = new JSONArray();
				
				for( Object userObject : userList) {
					JSONObject user = (JSONObject)userObject;
					JSONObject goods = (JSONObject)user.get("goods");
					if(user.get("userID").equals(userID) && goods.get(goodID)!=null)
						return true;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			    
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Not found");
		return false;
	}
}
