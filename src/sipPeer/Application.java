package sipPeer;

import java.util.*;
import javax.sip.*;

public class Application{
	public static void main(String[] args){
		System.out.println("hallo welt");

		SipFactory factory = SipFactory.getInstance();
		factory.setPathName("gov.nist");
		Properties sipProp = new Properties();
//		sipProp.setProperty("javax.sip.IP_ADDRESS", "129.6.55.181");
//		sipProp.setProperty("javax.sip.OUTBOUND_PROXY", "129.6.55.182:5070/UDP");
		sipProp.setProperty("javax.sip.STACK_NAME",
					"NISTv1.2");
		try{
			Application app = new Application(factory.createSipStack(sipProp));
			app.run();
		} catch(Exception ex){
			ex.printStackTrace();
		}		
	}

	public Application(SipStack sipStack){
	}

	public void run(){
	}
}
