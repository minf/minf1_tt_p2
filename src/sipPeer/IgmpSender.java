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
  private MessageProcessor messageProcessor;
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
      s.setSoTimeout(1000);
    }catch(Exception ex){
      messageProcessor.processError("IGMP initialization");
    }
  }

  public void join(){
    try{
      s.joinGroup(group);
    }catch(Exception ex){
      messageProcessor.processError("IGMP join");
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
      messageProcessor.processError("IGMP leave");
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

          messageProcessor.processInfo("Received multicast message: " + new String(messageIn.getData(), 0, messageIn.getLength()));
        } catch(Exception ex) {
        }  
      } else if(mode == IgmpSender.MODE_SERVER) {
        byte [] m = message.getBytes();
        DatagramPacket messageOut = new DatagramPacket(m, m.length, group, 9017);

        try{
          messageProcessor.processInfo("Sending: " + m.length + " bytes");

          s.send(messageOut);

          Thread.sleep(1000);
        } catch(Exception ex){
        }  
      } else {
        try {
          Thread.sleep(1000);
        } catch(Exception ex) {
        }
      }
    }
  }

/**/
  
  public void setMessageProcessor(MessageProcessor messageProcessor) {
    this.messageProcessor = messageProcessor;
  }
}


