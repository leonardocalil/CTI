package br.com.increaseit.cti;

import javax.telephony.callcenter.Agent;

import org.apache.log4j.Logger;

import com.avaya.jtapi.tsapi.LucentAddress;
import com.avaya.jtapi.tsapi.LucentAgent;
import com.avaya.jtapi.tsapi.LucentTerminal;
import com.avaya.jtapi.tsapi.LucentV7Agent;

public class AgentImpl {
	private static Logger logger = Logger.getLogger(AgentImpl.class);
	String agentId;
	Agent agent;
	LucentTerminal terminal;
	
	public AgentImpl(LucentTerminal terminal, LucentAddress address, String agentId, String agentPwd, boolean login) throws Exception{
		this.terminal = terminal;
		this.agentId = agentId;
		
		// se for para logar
		if (login){
			try {
				agent = terminal.addAgent(address, null, Agent.LOG_IN, LucentAgent.MODE_AUTO_IN, agentId, agentPwd);
			} catch (Exception e) {
				logger.error("Erro ao logar o agente "+this.agentId+": "+e.getMessage());
				
				//caso seja um agent invalido retorna o erro
				if (e.getMessage() != null && e.getMessage().contains("Invalid AgentId is specified")){
					throw e;
				}
				
				//caso ja esteja logado em outro ramal
				if (e.getMessage() != null && e.getMessage().contains("Agent is already logged into another Station")){
					throw e;
				}
				
				
				if (e.getMessage() != null && e.getMessage().contains("There is already an Agent logged on at the station")){
					String loggedInAgent = "";
					Agent[] agents = terminal.getAgents();
					
					if (agents.length > 0){
						loggedInAgent = agents[0].getAgentID();
						if (!loggedInAgent.equalsIgnoreCase(agentId)){
							throw e;
						}
					}
							
				}
			}	
		}
		
		
		try {
			if (agent==null) {
				if (terminal.getAgents() != null && terminal.getAgents().length > 0){
					agent = terminal.getAgents()[0];
				}
			}
//			agent.getACDAddress().addAddressListener(this);
		} catch (Exception e) {
			logger.error("Erro ao identificar o agente "+this.agentId+": "+e.getMessage());
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		agent = null;
	}

	public void logout() {
		try {
			terminal.removeAgent(agent);
		} catch (Exception e) {
			logger.error("Erro ao deslogar o agente "+this.agentId+": "+e.getMessage());
		}
	}
	
	public void setState(int state) {
		try {
			((LucentV7Agent)agent).setState(state,LucentV7Agent.MODE_AUTO_IN,0,(state==LucentV7Agent.WORK_NOT_READY)?true:false);
		} catch (Exception e) {
			logger.error("Erro ao alterar o status do agente "+this.agentId+": "+e.getMessage());
		}
	}

}
