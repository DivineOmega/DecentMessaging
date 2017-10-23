package main.network;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import main.Main;
import main.factory.MessageFactory;
import main.factory.PersonalFactory;
import main.record.MessageRecord;
import main.record.PersonalRecord;

import fi.iki.elonen.NanoHTTPD;


public class LocalWebServer extends NanoHTTPD 
{
	public LocalWebServer(int port)
	{
		super("localhost", port);
		
		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public Response serve(IHTTPSession session) {
				
		HashMap postData = new HashMap<String, String>();
		
		try {
			session.parseBody(postData);
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ResponseException e1) {
			e1.printStackTrace();
		}
		
		Map<String, List<String>> parameters = session.getParameters();
		
		JSONObject responseObj = new JSONObject();
		
		switch(session.getUri()) {
		
			case "/api/v1/address":
				if (session.getMethod() == Method.GET) {
					responseObj.put("status", "ok");
					responseObj.put("address", Main.dmAddress);
				} else {
					responseObj.put("status", "error");
					responseObj.put("description", "Invalid method. Must use GET method.");
				}
				break;
				
			case "/api/v1/personal-messages":
				
				if (session.getMethod() == Method.GET) {
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
				} else if (session.getMethod() == Method.POST) {
					if (parameters.containsKey("delete"))
					{
						int deleteId = 0;
						try {
							deleteId = Integer.parseInt(parameters.get("delete").get(0));
						} catch (NumberFormatException e) {
							responseObj.put("status", "error");
							responseObj.put("description", "Invalid 'delete' parameter. Must be an integer.");
							break;
						}
						
						PersonalRecord personalRecord = PersonalFactory.get(deleteId);
						if (personalRecord==null)
						{
							responseObj.put("status", "error");
							responseObj.put("description", "No personal message exists with the specified 'delete' parameter.");
							break;
						}
						boolean deleted = personalRecord.delete();
						if (!deleted) {
							responseObj.put("status", "error");
							responseObj.put("description", "Personal message could not be deleted.");
							break;
						} else {
							responseObj.put("status", "ok");
							responseObj.put("description", "Personal message deleted.");
							break;
						}
					} else {
						responseObj.put("status", "error");
						responseObj.put("description", "Missing parameter(s).");
						break;
					}
				} else {
					responseObj.put("status", "error");
					responseObj.put("description", "Invalid method. Must use GET or POST method.");
				}
				
			case "/api/v1/messages":
				
				if (session.getMethod() == Method.POST) {
					
					if (parameters.containsKey("recipientAddress") && parameters.containsKey("subject") && parameters.containsKey("body")) {
						
						String rcptAddress = parameters.get("recipientAddress").get(0);
						
						BigInteger rcptModulus = null;
						BigInteger rcptExponent = null;
						try {
							rcptModulus = Main.getModulusFromDmAddress(rcptAddress);
							rcptExponent = Main.getExponentFromDmAddress(rcptAddress);
						} catch (UnsupportedEncodingException e1) {
							responseObj.put("status", "error");
							responseObj.put("description", "Unsupported encoding exception.");
							break;
						}
						
						if (rcptModulus == null || rcptExponent == null) {
							responseObj.put("status", "error");
							responseObj.put("description", "Recipient DM address is not valid or supported.");
							break;
						}
						
						// Calculate recipient public key
						RSAPublicKeySpec keySpec = new RSAPublicKeySpec(rcptModulus, rcptExponent);
						KeyFactory fact;
						PublicKey rcptPublicKey;
						try 
						{
							fact = KeyFactory.getInstance("RSA");
							rcptPublicKey = fact.generatePublic(keySpec);
						} 
						catch (Exception e) 
						{
							responseObj.put("status", "error");
							responseObj.put("description", "Internal error calculating recipient public key.");
							break;
						}
						
						String subject = parameters.get("subject").get(0);
						String body = parameters.get("body").get(0);
						
						// Prefix body with subject
						body = subject + "\n" + body;
						
						// Prefix body with DM Address
						body = Main.dmAddress + "\n" + body;
						
						// Create new message
						MessageRecord newMsg = null;
						try {
							newMsg = MessageFactory.encryptContentAndCreateNew(body.getBytes("UTF-8"), rcptPublicKey);
						} catch (Exception e) {
							responseObj.put("status", "error");
							responseObj.put("description", "Internal error creating new message.");
							break;
						} 
						
						if (newMsg==null) {
							responseObj.put("status", "error");
							responseObj.put("description", "Internal error verifying message creation.");
							break;
						}
						
						responseObj.put("status", "ok");
						responseObj.put("description", "Message queued for delivery.");
						break;
						
						
					} else {
						responseObj.put("status", "error");
						responseObj.put("description", "Missing parameter(s).");
						break;
					}
					
				} else {
					responseObj.put("description", "Invalid method. Must use POST method.");
				}
				
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
