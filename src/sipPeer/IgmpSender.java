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
	private boolean stop, send;
	private String message;
	InetAddress group;
    
	public IgmpSender(){
		try{
		group =  InetAddress.getByName("239.238.237.17");
		s = new MulticastSocket(9017);
		s.setSoTimeout(100);
		}catch(Exception ex){
		}
	}

	public void join(){
		try{
		s.joinGroup(group);
		}catch(Exception ex){
		}
	}

	public void leave(){
		try{
		s.leaveGroup(group);
		}catch(Exception ex){
		}
	}

	public void stopRunning(){
		stop = true;
	}

	public void sendMessage(String message){
		this.message = message;
		send = true;
	}

	public void run(){
		while(!stop){
			try{
				byte[] buffer = new byte[256];
				DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
				s.receive(messageIn);
				System.out.println("Received: " + new String(messageIn.getData()));
			} catch(Exception ex){
				
			}	
			
			if(send){
				byte [] m = message.getBytes();
				DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 9017);
				try{
				  s.send(messageOut);
				} catch(Exception ex){
				
				}	
						
	send = false;
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


