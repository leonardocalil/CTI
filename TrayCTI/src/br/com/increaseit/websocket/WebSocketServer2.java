package br.com.increaseit.websocket;

import java.io.IOException;
import java.net.ServerSocket;

public class WebSocketServer2 implements Runnable{

	private ServerSocket listener ;
	private int port;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	public WebSocketServer2() throws IOException {
		
		this.port = 80;
		listener  = new ServerSocket(this.port);
	}


	public WebSocketServer2(int port) throws IOException {
		this.port = port;
		listener  = new ServerSocket(port);
		
	}


	public ServerSocket getServer() {
		return listener ;
	}


	public void setServer(ServerSocket server) {
		this.listener  = server;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		int clientNumber = 0;
		System.out.println("WebSocketServer:run...");
		try {
            while (true) {
                try {
					new WebSocketClient(listener.accept(), clientNumber++).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        } finally {
            try {
				listener.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	
	
	

}
