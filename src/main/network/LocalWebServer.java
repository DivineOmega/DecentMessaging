package main.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import main.Main;
import main.factory.PersonalFactory;
import main.record.PersonalRecord;

import fi.iki.elonen.NanoHTTPD;


public class LocalWebServer extends NanoHTTPD 
{
	public LocalWebServer(int port)
	{
		super(port);
		
		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public Response serve(IHTTPSession session) {
		
		Map<String, List<String>> parameters = session.getParameters();
		
		JSONObject responseObj = new JSONObject();
		
		switch(session.getUri()) {
		
			case "/api/v1/address":
				
				responseObj.put("status", "ok");
				responseObj.put("address", Main.dmAddress);
				break;
				
			case "/api/v1/personal-messages":
				
				if (parameters.containsKey("id")) {
					
					int id = 0;
					
					try {
						id = Integer.parseInt(parameters.get("id").get(0));
					} catch (NumberFormatException e) {
						responseObj.put("status", "error");
						responseObj.put("description", "Invalid 'id' parameter. Must be an integer.");
						break;
					}
					
					PersonalRecord personalRecord = PersonalFactory.get(id);
					if (personalRecord==null)
					{
						responseObj.put("status", "error");
						responseObj.put("description", "No personal message exists with the specified 'id' parameter.");
						break;
					}
					
					responseObj.put("status", "ok");
					responseObj.put("datetime", personalRecord.originally_received.getTime());
					responseObj.put("subject", personalRecord.subject);
					
					try {
						responseObj.put("body", personalRecord.getContentAsString());
					} catch (IOException e) {
						responseObj.clear();
						responseObj.put("status", "error");
						responseObj.put("description", "Internal error retrieved personal message body content.");
						break;
					}
					
					try {
						responseObj.put("from", Main.createDmAddress(personalRecord.publickey_modulus, personalRecord.publickey_exponent));
					} catch (UnsupportedEncodingException e) {
						responseObj.clear();
						responseObj.put("status", "error");
						responseObj.put("description", "Internal error retrieved personal message from address.");
						break;
					}
					
					break;
				}
				
				long timestamp = 0;
				
				if (parameters.containsKey("since"))
				{
					try {
						timestamp = Integer.parseInt(parameters.get("since").get(0));
					} catch (NumberFormatException e) {
						responseObj.put("status", "error");
						responseObj.put("description", "Invalid 'since' parameter. Must be an integer.");
						break;
					}
				}
				
				ArrayList<Integer> ids = PersonalFactory.getIDsAfterTimestamp(new java.sql.Timestamp(timestamp));
				
				responseObj.put("status", "ok");
				responseObj.put("ids", ids);
				break;
				
			default:
				responseObj.put("status", "error");
				responseObj.put("description", "Invalid URI.");
				break;
		}
		
		
		Response response = newFixedLengthResponse(responseObj.toJSONString());
		
		response.setMimeType(MIME_PLAINTEXT);
				
		return response;
		
	}
	
}
