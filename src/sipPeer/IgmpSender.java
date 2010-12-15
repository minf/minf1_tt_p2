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
  private int mode;
	private String message = "muhkuh";
	InetAddress group;
  
  private static final int MODE_NONE = 0;
  private static final int MODE_SERVER = 1;
  private static final int MODE_CLIENT = 2; 

	public IgmpSender(){
    mode = IgmpSender.MODE_NONE;
 
		try{
      group = InetAddress.getByName("239.238.237.17");
      s = new MulticastSocket(9017);
		}catch(Exception ex){
      System.out.println("ERROR IGMP");
		}
	}

	public void join(){
		try{
      s.joinGroup(group);
		}catch(Exception ex){
      ex.printStackTrace();
		}
	}

  public void setServer() {
    mode = IgmpSender.MODE_SERVER;
  }

  public void setClient() {
    mode = IgmpSender.MODE_CLIENT;
  }

	public void leave(){
		try{
      s.leaveGroup(group);
		}catch(Exception ex){
      ex.printStackTrace();
		}
	}

	public void stopRunning(){
		mode = IgmpSender.MODE_NONE;
	}

	public void run(){
		while(true) {
      if(mode == IgmpSender.MODE_CLIENT) {
        try {
          byte[] buffer = new byte[256];

          DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);

          s.receive(messageIn);

          System.out.println("Received: " + new String(messageIn.getData()));
        } catch(Exception ex){
          System.out.println("Receive failed");	
        }	
		  } else if(mode == IgmpSender.MODE_SERVER) {
				byte [] m = message.getBytes();
				DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 9017);

				try{
				  System.out.println("Trying to send " + m.length + " bytes");

				  s.send(messageOut);

          Thread.sleep(1000);
				} catch(Exception ex){
				  System.err.println("Failed to send: " + ex.getMessage());
				}	
			}
		}
	}
}


