package br.com.increaseit.util;

import org.java_websocket.WebSocket;

public class User {

	private String login; //User Login G00XXXXX
	private String agentId; //the AgentId
	private String station; //Ramal
	private String hostname; //The hostname
	private WebSocket conn;
	
	public User(){
		
	}
	
	public User(String login, String agentId, String station, String hostname,WebSocket conn){
		this.login = login;
		this.agentId = agentId;
		this.station = station;
		this.hostname = hostname;
		this.conn = conn;
	}
	
	
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	public String getStation() {
		return station;
	}
	public void setStation(String station) {
		this.station = station;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public WebSocket getConn() {
		return conn;
	}

	public void setConn(WebSocket conn) {
		this.conn = conn;
	}
	
	
	
}
