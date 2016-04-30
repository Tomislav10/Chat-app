package Moj2;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
@ServerEndpoint("/chatroomServerEndpoint")
public class ChatroomServerEndpoint {
	static Set<Session> chatroomUsers = Collections.synchronizedSet(new HashSet<Session>());
	@OnOpen
	public void handleOpen(Session userSession) throws IOException {
		chatroomUsers.add(userSession);
		Iterator<Session> iterator = chatroomUsers.iterator();
        while (iterator.hasNext()) (iterator.next()).getBasicRemote().sendText(buildJsonUsersData());
	}
	@OnMessage
	public void handleMessage(String message, Session userSession) throws IOException {
		String username = (String) userSession.getUserProperties().get("username");
		Iterator<Session> iterator = chatroomUsers.iterator();
		if (username==null) {
			userSession.getUserProperties().put("username", message);
			userSession.getBasicRemote().sendText(buildJsonMessageData("System","you are now connected as "+message));
			while (iterator.hasNext()) (iterator.next()).getBasicRemote().sendText(buildJsonUsersData());
		} else {
			
			while (iterator.hasNext()) iterator.next().getBasicRemote().sendText(buildJsonMessageData(username, message));
		}
	}
	@OnClose
	public void handleClose(Session userSession) throws IOException {
		chatroomUsers.remove(userSession);
		Iterator<Session> iterator = chatroomUsers.iterator();
        while (iterator.hasNext()) (iterator.next()).getBasicRemote().sendText(buildJsonUsersData());
	}
	private  String buildJsonUsersData() { 
		Iterator<String> iterator = getUserNames().iterator();
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		while (iterator.hasNext()) jsonArrayBuilder.add((String)iterator.next());
		return Json.createObjectBuilder().add("users", jsonArrayBuilder).build().toString();
	}
	
	private String buildJsonMessageData(String username, String message) {  
		JsonObject jsonObject = Json.createObjectBuilder().add("message", username+": "+message).build();
		StringWriter stringWriter = new StringWriter();
		try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {jsonWriter.write(jsonObject);}
		return stringWriter.toString();
	}
	private Set<String> getUserNames(){
		HashSet<String> returnSet = new HashSet<String>();
		Iterator<Session> iterator = chatroomUsers.iterator();
    	while (iterator.hasNext()) returnSet.add(iterator.next().getUserProperties().get("username").toString());
		return returnSet;
	}
}
