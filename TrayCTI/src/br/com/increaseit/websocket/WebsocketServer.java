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

public class WebsocketServer extends WebSocketServer{

	private Set<WebSocket> conns;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	public WebsocketServer() {
		
		super(new InetSocketAddress(90));
        conns = new HashSet<>();
	}


	public WebsocketServer(int port) throws IOException {
		super(new InetSocketAddress(port));
        conns = new HashSet<>();
		
	}



	@Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conns.add(conn);
        System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        conns.remove(conn);
        System.out.println("Closed connection to " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Message from client: " + message);
        
		JSONObject obj = new JSONObject(message);
		String action = obj.getString("action");
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
		}
		if(action.equalsIgnoreCase("answerCall")) {
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
		}
		if(action.equalsIgnoreCase("disconnectCall")) {
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
		}
        		
        
        //conn.send("Recebi a sua mensage: "+message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        //ex.printStackTrace();
        if (conn != null) {
            conns.remove(conn);
            // do some thing if required
        }
        System.out.println("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

	
	
	
	

}
