package Moj2;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.Session;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;

@ServerEndpoint("/ChatroomEndPoint")

public class ChatroomEndPoint {
    static Set<Session> ChatroomUsers = Collections.synchronizedSet(new HashSet<Session>());
    @OnOpen
    public void handleOpen(Session userSession){
    	ChatroomUsers.add(userSession);
    }
    @OnClose
    public void handleClose(Session userSession){
    	ChatroomUsers.remove(userSession);
    }
    @OnMessage
    public void handleMessage(String message, Session userSession) throws IOException {
    	String username = (String) userSession.getUserProperties().get("userName");
    	if(username == null){
    		userSession.getUserProperties().put("userName", message);
    		userSession.getBasicRemote().sendText(buildJsonData("System","You are connected as"+message));
    	}else{
    		Iterator<Session> iterator= ChatroomUsers.iterator();
    		while(iterator.hasNext()){
    			iterator.next().getBasicRemote().sendText(buildJsonData(username,message));
    		}
    	}
    }
	private String buildJsonData(String username, String message) {
			JsonObject jsonObject = Json.createObjectBuilder().add("message", username + ":"+message).build();
		    StringWriter stringWriter = new StringWriter();
		    try (JsonWriter jsonWriter = Json.createWriter(stringWriter)){
		    	jsonWriter.write(jsonObject);
		    }
			return stringWriter.toString();
	}
	
}
