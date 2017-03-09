package br.com.increaseit.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.increaseit.frontend.TrayIconCTI;
import br.com.increaseit.util.User;

public class WebsocketServer extends WebSocketServer{

	private Set<User> users;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	public WebsocketServer() {
		
		super(new InetSocketAddress(90));
		users = new HashSet<>();
	}


	public WebsocketServer(int port) throws IOException {
		super(new InetSocketAddress(port));
		users = new HashSet<>();
		
	}



	@Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
		User user = new User();
		user.setConn(conn);
		user.setAgentId(TrayIconCTI.ctiConnector.getAgentId());
		user.setStation(TrayIconCTI.ctiConnector.getDeviceId());
        users.add(user);
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    	for(User user : users) {
    		if(user.getConn() ==  conn) {
    			users.remove(user);    			
    			break;
    		}
    	}        
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);
        
		JSONObject obj = new JSONObject(message);
		String action = obj.getString("action");
		
		User user = null;
		for(User us : users) {
			if(us.getConn() == conn) {
				user = us;
			}
		}
		
		if(action.equalsIgnoreCase("makeCall")) {
			try {
				TrayIconCTI.ctiConnector.makeCall(obj.getString("station"), obj.getString("dialednum"));
				conn.send("0");
			} catch (RemoteException e) {
				conn.send("-1");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				conn.send("-1");
				e.printStackTrace();
			}
		} else if(action.equalsIgnoreCase("answerCall")) {
			try {
				TrayIconCTI.ctiConnector.answerCall(obj.getString("station"));
				conn.send("0");
			} catch (RemoteException e) {
				conn.send("-1");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				conn.send("-1");
				e.printStackTrace();
			}
		} else  if(action.equalsIgnoreCase("disconnectCall")) {
			try {
				TrayIconCTI.ctiConnector.releaseCall(obj.getString("station"));
				conn.send("0");
			} catch (RemoteException e) {
				conn.send("-1");
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				conn.send("-1");
				e.printStackTrace();
			}
		} else if(action.equalsIgnoreCase("login")) {			
			try {
				TrayIconCTI.ctiConnector.login(obj.getString("agent"), null, obj.getString("station"));
				if(user != null) {
					if(obj.getString("station") != null && obj.getString("station").length() > 0 &&
							obj.getString("agent") != null && obj.getString("agent").length() > 0) {
						user.setStation(obj.getString("station"));
						user.setAgentId(obj.getString("agent"));
					}
				}								
				conn.send("Login [Agente: " + obj.getString("agent") + " | Ramal: " + obj.getString("station") + "]");
			} catch(Exception e) {
				conn.send(e.getMessage());
			}
		} else if(action.equalsIgnoreCase("logout")) {
			try {
				TrayIconCTI.ctiConnector.logout();
				if(user != null) {
					user.setAgentId("");
				}
			} catch(Exception e) {
				conn.send(e.getMessage());
			}
		}
        		
        
        //conn.send("Recebi a sua mensage: "+message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        //ex.printStackTrace();
        if (conn != null) {
        	for(User user : users) {
        		if(user.getConn() == conn) {
        			users.remove(user);
        			break;
        		}
        	}
        	System.out.println("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
        }
        
    }
    
    public void send(String station, String msg) {
    	for(User user : users) {
    		if(station.equals(user.getStation())) {
    			if(user.getConn() != null ) {
    				user.getConn().send(msg);
    			}
    		}
    	}    	
    	
    }


	public Set<User> getUsers() {
		return users;
	}
    
    

	
	
	
	

}
