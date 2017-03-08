package br.com.increaseit.cti;

import java.util.Observable;

import javax.telephony.Address;
import javax.telephony.Call;
import javax.telephony.CallEvent;
import javax.telephony.ConnectionEvent;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.MetaEvent;
import javax.telephony.Provider;
import javax.telephony.ProviderEvent;
import javax.telephony.ProviderListener;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;
import javax.telephony.TerminalConnection;
import javax.telephony.TerminalConnectionEvent;
import javax.telephony.callcontrol.CallControlCall;
import javax.telephony.callcontrol.CallControlConnectionEvent;
import javax.telephony.callcontrol.CallControlTerminalConnection;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;
import javax.telephony.callcontrol.CallControlTerminalConnectionListener;

import com.avaya.jtapi.tsapi.LucentAddress;
import com.avaya.jtapi.tsapi.LucentTerminal;

import br.com.increaseit.frontend.TrayIconCTI;


public class CtiConnector extends Observable implements CallControlTerminalConnectionListener, ProviderListener {

	java.lang.String providerString = "AVAYA#CMCTB#CSTA#SVUXPAES2;loginID=siebel_user;passwd=Gvt@2011;";
	
	private Provider provider = null;
	
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;
	public static final String DATEFORMAT_JAVA = "dd/MM/yyyy kk:mm:ss";
	public static final String DATEFORMAT_ORACLE = "DD/MM/YYYY HH24:MI:SS";
	
	private String deviceId;
	
	private Address myAddress;
	private Terminal myTerminal;
	private Terminal myTerminalEvent;
	
	AgentImpl myAgent;
	
	
	public CtiConnector() {
		try {
			provider = JtapiPeerFactory.getJtapiPeer("com.avaya.jtapi.tsapi.TsapiPeer").getProvider(providerString);
			provider.addProviderListener(this);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	
	}
		
	
	
	public class PredictiveCallOut {
		int errorCode;
		String errorDescription;
		String ucid;
	}
	

	public void makeCall(java.lang.String extension, java.lang.String destinationNumber) throws java.rmi.RemoteException {
    	
    	Call call = null;
    	
    	try {			    
    		call = provider.createCall();
        	call.connect(provider.getTerminal(extension), provider.getAddress(extension), destinationNumber);
    	} catch (Exception e) {
			e.printStackTrace();
    	} finally {
    		call = null;    		
		}
    }

    public void releaseCall(java.lang.String extension) throws java.rmi.RemoteException {
    	
    	TerminalConnection connList[] = null;
    	try {
			connList = provider.getTerminal(extension).getTerminalConnections();
			if (connList != null) {
				for (int i = 0; i < connList.length; i++){
					if (((CallControlTerminalConnection) connList[i]).getCallControlState() == CallControlTerminalConnection.TALKING){
						connList[i].getConnection().disconnect();
						break;
					}
				}
			}
    	} catch (Exception e) {
			e.printStackTrace();
    	} finally {
    		connList = null;    		
		}
    }

    public void answerCall(java.lang.String extension) throws java.rmi.RemoteException {
    	TerminalConnection connList[] = null;
    	try {
			connList = provider.getTerminal(extension).getTerminalConnections();
			if (connList != null) {
				for (int i = 0; i < connList.length; i++){
					if (((CallControlTerminalConnection) connList[i]).getCallControlState() == CallControlTerminalConnection.RINGING){
						connList[i].answer();
						break;
					}
				}
			}
    	} catch (Exception e) {
			e.printStackTrace();
    	} finally {
    		connList = null;
		}
    }

    public void transferCall(java.lang.String extension, java.lang.String destinationNumber) throws java.rmi.RemoteException {
		Call activeCall;
		Call transferCall;
		TerminalConnection connList[] = null;
		try {
			connList = provider.getTerminal(extension).getTerminalConnections();
			if (connList != null) {
				for (int i = 0; i < connList.length; i++) {
					if (((CallControlTerminalConnection) connList[i]).getCallControlState() == CallControlTerminalConnection.TALKING) {
						activeCall = connList[i].getConnection().getCall();
						((CallControlTerminalConnection)connList[i]).hold();
			        	transferCall = provider.createCall();
						transferCall.connect(provider.getTerminal(extension), provider.getAddress(extension), destinationNumber);
						((CallControlCall)transferCall).transfer(activeCall);
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			activeCall = null;
			transferCall = null;
			connList = null;
		}
    }

    public void holdCall(java.lang.String extension) throws java.rmi.RemoteException {

		TerminalConnection connList[] = null;
		try {

			connList = provider.getTerminal(extension).getTerminalConnections();
			if (connList != null) {
				for (int i = 0; i < connList.length; i++) {
					if (((CallControlTerminalConnection) connList[i]).getCallControlState() == CallControlTerminalConnection.TALKING) {
						((CallControlTerminalConnection) connList[i]).hold();
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connList = null;
		}
    }

    public void unHoldCall(java.lang.String extension) throws java.rmi.RemoteException {

		TerminalConnection connList[] = null;
		int unHoldCall = -1;
		boolean isTalking = false;
		try {
			connList = provider.getTerminal(extension).getTerminalConnections();
			if (connList != null) {
				for (int i = 0; i < connList.length; i++) {
					if (((CallControlTerminalConnection) connList[i]).getCallControlState() == CallControlTerminalConnection.HELD) {
						unHoldCall = i;
					}
					if (((CallControlTerminalConnection) connList[i]).getCallControlState() == CallControlTerminalConnection.TALKING) {
						isTalking = true;
						break;
					}
				}
			}
			if ((!isTalking) && (unHoldCall >= 0)) {
				((CallControlTerminalConnection) connList[unHoldCall]).unhold();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connList = null;
		}
    }
    
    public void logout() {
    	try {

			try {
				myAgent.logout();
				myAgent = null;
				deviceId = null;
				//((LucentTerminal) myTerminal).removeAgent(myAgent);
			} catch (Exception e) {
				TrayIconCTI.server.send("Erro ao executar comando de logout: "+e.getMessage());
				System.out.println("Erro ao executar comando de logout: "+e.getMessage());
			}
			try {
				myTerminalEvent.removeCallListener(this);				
			} catch (Exception e) {
				TrayIconCTI.server.send("Erro ao retirar monitoracao: "+e.getMessage());
				System.out.println("Erro ao retirar monitoracao: "+e.getMessage());
			}

		} catch (Exception e) {
			TrayIconCTI.server.send("Erro ao efetuar logout: "+e.getMessage());
			System.out.println("Erro ao efetuar logout: "+e.getMessage());			
		}
    }
    public void login(String agentId, String agentPwd, String deviceId) {
		try {
			//this.loggedInUser = new User(login, agentId, deviceId, hostName);

			this.deviceId = deviceId;
			
			myAddress = provider.getAddress(deviceId);
			
			
			
			try{
				myTerminal = provider.getTerminal(deviceId);
			}catch(Exception e){
				
				//caso seja um ramal inválido
				if (e.getMessage() != null && e.getMessage().contains("device is not a terminal")){
					System.out.println("Ramal Inválido: " + deviceId);
					TrayIconCTI.server.send("Ramal Inválido: " + deviceId);					
					return;	
				}
				

				
			}
			
			myTerminalEvent = provider.getTerminal(deviceId);
			myTerminalEvent.addCallListener(this);
			
			try {
				myAgent = new AgentImpl((LucentTerminal)myTerminal, (LucentAddress)myAddress, agentId, agentPwd, true);
			} catch (Exception e) {
				
				//caso seja um agent invalido retorna o erro
				if (e.getMessage() != null && e.getMessage().contains("Invalid AgentId is specified")){
					
					System.out.println("Agente Inválido: " + agentId);
					TrayIconCTI.server.send("Agente Inválido: " + agentId);
					
					return;
				}
				
				//caso ja alguem esteja logado neste ramal
				if (e.getMessage() != null && e.getMessage().contains("There is already an Agent logged on at the station")){
					
					System.out.println("Já existe um agente logado neste ramal.");
					TrayIconCTI.server.send("Já existe um agente logado neste ramal.");
					
					return;	
				}
				
				//caso ja esteja logado em outro ramal
				if (e.getMessage() != null && e.getMessage().contains("Agent is already logged into another Station")){
					
					System.out.println("Este agente já está logado em outro ramal.");
					TrayIconCTI.server.send("Este agente já está logado em outro ramal.");
					
					return;	
				}
			}

			TrayIconCTI.server.send("Login [Agente: " + agentId + " | Ramal: " + deviceId + "]");

		} catch (ResourceUnavailableException e) {
			System.out.println("### ResourceUnavailableException ### " + e.getMessage());
			TrayIconCTI.server.send("### ResourceUnavailableException ### " + e.getMessage());
		} catch (Exception e) {
			System.out.println("AgentId: " + agentId + " - DeviceId: " + deviceId+ " - " + e.getMessage());
			TrayIconCTI.server.send("AgentId: " + agentId + " - DeviceId: " + deviceId+ " - " + e.getMessage());
		}
		return;
	}
    
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		provider.shutdown();
		
		 
	}

	@Override
	public void providerEventTransmissionEnded(ProviderEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("providerEventTransmissionEnded");
		
	}

	@Override
	public void providerInService(ProviderEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("providerInService");
	}

	@Override
	public void providerOutOfService(ProviderEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("providerOutOfService");
	}

	@Override
	public void providerShutdown(ProviderEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("providerShutdown");
		
	}

	@Override
	public void connectionAlerting(CallControlConnectionEvent arg0) {
		System.out.println("connectionAlerting");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionDialing(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionDialing");
	}

	@Override
	public void connectionDisconnected(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionDisconnected");
	}

	@Override
	public void connectionEstablished(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionEstablished");
	}

	@Override
	public void connectionFailed(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionFailed");
	}

	@Override
	public void connectionInitiated(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionInitiated");
		
	}

	@Override
	public void connectionNetworkAlerting(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionNetworkAlerting");
	}

	@Override
	public void connectionNetworkReached(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionNetworkReached");
	}

	@Override
	public void connectionOffered(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionOffered");
	}

	@Override
	public void connectionQueued(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionQueued");
		
	}

	@Override
	public void connectionUnknown(CallControlConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionUnknown");
	}

	@Override
	public void callActive(CallEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("callActive");
		
	}

	@Override
	public void callEventTransmissionEnded(CallEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("callEventTransmissionEnded");
	}

	@Override
	public void callInvalid(CallEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("callInvalid");
	}

	@Override
	public void multiCallMetaMergeEnded(MetaEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("multiCallMetaMergeEnded");
	}

	@Override
	public void multiCallMetaMergeStarted(MetaEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("multiCallMetaMergeStarted");
	}

	@Override
	public void multiCallMetaTransferEnded(MetaEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("multiCallMetaTransferEnded");
	}

	@Override
	public void multiCallMetaTransferStarted(MetaEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("multiCallMetaTransferStarted");
	}

	@Override
	public void singleCallMetaProgressEnded(MetaEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("singleCallMetaProgressEnded");
	}

	@Override
	public void singleCallMetaProgressStarted(MetaEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("singleCallMetaProgressStarted");
	}

	@Override
	public void singleCallMetaSnapshotEnded(MetaEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("singleCallMetaSnapshotEnded");
	}

	@Override
	public void singleCallMetaSnapshotStarted(MetaEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("singleCallMetaSnapshotStarted");
	}

	@Override
	public void connectionAlerting(ConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionAlerting");
	}

	@Override
	public void connectionConnected(ConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionConnected");
	}

	@Override
	public void connectionCreated(ConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionCreated");
	}

	@Override
	public void connectionDisconnected(ConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionDisconnected");
	}

	@Override
	public void connectionFailed(ConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionFailed");
	}

	@Override
	public void connectionInProgress(ConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionInProgress");
	}

	@Override
	public void connectionUnknown(ConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("connectionUnknown");
	}

	@Override
	public void terminalConnectionActive(TerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionActive");
	}

	@Override
	public void terminalConnectionCreated(TerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionCreated");
	}

	@Override
	public void terminalConnectionDropped(TerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionDropped");
	}

	@Override
	public void terminalConnectionPassive(TerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionPassive");
	}

	@Override
	public void terminalConnectionRinging(TerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionRinging");
	}

	@Override
	public void terminalConnectionUnknown(TerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionUnknown");
	}

	@Override
	public void terminalConnectionBridged(CallControlTerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionBridged");
	}

	@Override
	public void terminalConnectionDropped(CallControlTerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionDropped");
	}

	@Override
	public void terminalConnectionHeld(CallControlTerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionHeld");
	}

	@Override
	public void terminalConnectionInUse(CallControlTerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionInUse");
	}

	@Override
	public void terminalConnectionRinging(CallControlTerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionRinging");
	}

	@Override
	public void terminalConnectionTalking(CallControlTerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionTalking");
	}

	@Override
	public void terminalConnectionUnknown(CallControlTerminalConnectionEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("terminalConnectionUnknown");
	}

	
	

}
