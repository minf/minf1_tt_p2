package sipPeer;

import java.net.*;
import java.text.*;
import java.util.*;
import javax.sip.*;
import javax.sip.header.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import dev2dev.textclient.*;

public class IgmpSender implements Runnable
{
	private MulticastSocket s;
	private boolean stop, send,joined;
	private String message = "muhkuh";
	InetAddress group;
    
	public IgmpSender(){
		try{
      group = InetAddress.getByName("239.238.237.17");
      s = new MulticastSocket(9017);
      s.setSoTimeout(750);
		}catch(Exception ex){
      System.out.println("ERROR IGMP");
		}
	}

  public void beginSending() {
    send = true;
  }

  public void stopSending() {
    send = false;
  }

	public void join(){
		if(joined) return;
		try{
      s.joinGroup(group);
      joined = true;
		}catch(Exception ex){
      System.out.println("ERROR JOIN");
		}
	}

	public void leave(){
                if(!joined) return;
		try{
                 s.leaveGroup(group);
                 joined = false;
		}catch(Exception ex){
      System.out.println("ERROR LEAVE");
		}
	}

	public void stopRunning(){
		stop = true;
	}

	public void run(){
		while(!stop){
			try{
				byte[] buffer = new byte[256];
				DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
				s.receive(messageIn);
				System.out.println("Received: " + new String(messageIn.getData()));
			} catch(Exception ex){
        // bla	
			}	
			
			if(send){
				byte [] m = message.getBytes();
				DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 9017);
				try{
				  System.out.println("Trying to send " + m.length + " bytes");
				  s.send(messageOut);
				} catch(Exception ex){
				  System.err.println("Failed to send: " + ex.getMessage());
				}	
						
        //send = false;
			}
		}
	}
// get messages from others in group
// byte[] buffer = new byte[1000];
// for(int i=0; i< 3; i++) {
// DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
// s.receive(messageIn);
// System.out.println("Received:" + new String(messageIn.getData()));
// }
// s.leaveGroup(group);
//
}


