package br.com.increaseit.cti;

import javax.telephony.Call;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.Provider;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlCall;
import javax.telephony.callcontrol.CallControlTerminalConnection;

import com.avaya.jtapi.tsapi.LucentAddress;
import com.avaya.jtapi.tsapi.LucentCall;
import com.avaya.jtapi.tsapi.LucentTerminal;
import com.avaya.jtapi.tsapi.LucentV5CallInfo;
import com.avaya.jtapi.tsapi.UserToUserInfo;


public class CtiConnector {

	java.lang.String providerString = "AVAYA#CMCTB#CSTA#SVUXPAES2;loginID=siebel_user;passwd=Gvt@2011;";
	
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;
	public static final String DATEFORMAT_JAVA = "dd/MM/yyyy kk:mm:ss";
	public static final String DATEFORMAT_ORACLE = "DD/MM/YYYY HH24:MI:SS";
	
	public CtiConnector() {
	
	}
		
	
	
	public class PredictiveCallOut {
		int errorCode;
		String errorDescription;
		String ucid;
	}
	

	public void makeCall(java.lang.String extension, java.lang.String destinationNumber) throws java.rmi.RemoteException {
    	Provider provider = null;
    	Call call = null;
    	
    	try {			
    		provider = JtapiPeerFactory.getJtapiPeer("com.avaya.jtapi.tsapi.TsapiPeer").getProvider(providerString);
    		call = provider.createCall();
        	call.connect(provider.getTerminal(extension), provider.getAddress(extension), destinationNumber);
    	} catch (Exception e) {
			e.printStackTrace();
    	} finally {
    		call = null;
    		provider.shutdown();
    		provider = null;
		}
    }

    public void releaseCall(java.lang.String extension) throws java.rmi.RemoteException {
    	Provider provider = null;
    	TerminalConnection connList[] = null;
    	try {
			provider = JtapiPeerFactory.getJtapiPeer("com.avaya.jtapi.tsapi.TsapiPeer").getProvider(providerString);
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
    		provider.shutdown();
    		provider = null;
		}
    }

    public void answerCall(java.lang.String extension) throws java.rmi.RemoteException {
    	Provider provider = null;
    	TerminalConnection connList[] = null;
    	try {
			provider = JtapiPeerFactory.getJtapiPeer("com.avaya.jtapi.tsapi.TsapiPeer").getProvider(providerString);
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
    		provider.shutdown();
    		provider = null;
		}
    }

    public void transferCall(java.lang.String extension, java.lang.String destinationNumber) throws java.rmi.RemoteException {
		Call activeCall;
		Call transferCall;
		Provider provider = null;
		TerminalConnection connList[] = null;
		try {
			provider = JtapiPeerFactory.getJtapiPeer("com.avaya.jtapi.tsapi.TsapiPeer").getProvider(providerString);
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
			provider.shutdown();
			provider = null;
		}
    }

    public void holdCall(java.lang.String extension) throws java.rmi.RemoteException {
		Provider provider = null;
		TerminalConnection connList[] = null;
		try {
			provider = JtapiPeerFactory.getJtapiPeer("com.avaya.jtapi.tsapi.TsapiPeer").getProvider(providerString);
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
			provider.shutdown();
			provider = null;
		}
    }

    public void unHoldCall(java.lang.String extension) throws java.rmi.RemoteException {
		Provider provider = null;
		TerminalConnection connList[] = null;
		int unHoldCall = -1;
		boolean isTalking = false;
		try {
			provider = JtapiPeerFactory.getJtapiPeer("com.avaya.jtapi.tsapi.TsapiPeer").getProvider(providerString);
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
			provider.shutdown();
			provider = null;
		}
    }
	public java.lang.String teste1(java.lang.String in) throws java.rmi.RemoteException {
		String extension = "2203";
		String destinationNumber = "2707";
		UserToUserInfo avayaUUI;
		
		Provider provider = null;
		LucentCall activeCall;
		LucentCall transferCall;
		TerminalConnection connList[] = null;
		try {
			provider = JtapiPeerFactory.getJtapiPeer("com.avaya.jtapi.tsapi.TsapiPeer").getProvider(providerString);
			connList = ((LucentTerminal)provider.getTerminal(extension)).getTerminalConnections();
			if (connList != null) {
				for (int i = 0; i < connList.length; i++) {
					if (((CallControlTerminalConnection) connList[i]).getCallControlState() == CallControlTerminalConnection.TALKING) {
						activeCall = (LucentCall)(((CallControlTerminalConnection) connList[i]).getConnection()).getCall();
						String tmp = ((LucentV5CallInfo)activeCall).getUCID();
			        	avayaUUI = new UserToUserInfo(tmp);
						((CallControlTerminalConnection)connList[i]).hold();
			        	transferCall = (LucentCall)provider.createCall();
						((LucentCall)transferCall).connect((LucentTerminal)provider.getTerminal(extension), (LucentAddress)provider.getAddress(extension), 
								destinationNumber, true, avayaUUI);
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
			provider.shutdown();
			provider = null;
		}
		return "Retorno: "+in;
    }


}
