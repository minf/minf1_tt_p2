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

public class SipPeer 
	extends JFrame
	implements MessageProcessor,
		ActionListener{
	
	public static void main(String[] args){
		System.out.println("hallo welt");

		if(args.length!=2){
			System.out.println("usage: java -jar SipPeer.jar dest port");
			System.exit(0);
		}
		try{
			String dest = args[0];
			int port = Integer.parseInt(args[1]);
			String ip = InetAddress.getLocalHost().getHostAddress();

			SipLayer sipLayer = new SipLayer(dest, ip, port);
			
			SipPeer app = new SipPeer(sipLayer);
			sipLayer.register();
			app.show();
		} catch(Exception ex){
			ex.printStackTrace();
		}		
	}

	private SipLayer sipLayer;
	private JTextField dest;
	private JButton register,bye,call,cancel;
	private JTextPane log;
	private StyledDocument logdoc;
	private ClientTransaction inviteTransaction;
	private SimpleDateFormat logTimeFormat =
            new SimpleDateFormat("HH:mm:ss.SSS");
	private IgmpSender igmp = new IgmpSender();
	private Thread igmpThread;

	public SipPeer(SipLayer sipLayer){
		super("SipPeer");
		
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				e.getWindow().dispose();
				System.exit(0);
			}		
		});

		this.sipLayer = sipLayer;
		this.sipLayer.setMessageProcessor(this);

		igmpThread = new Thread(igmp);
		igmpThread.start();

		initUI();
	}

	private void initUI(){
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.gridwidth = c.gridheight = 1;
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = c.ipady = 4;

		setLayout(grid);

		c.gridwidth=4;
		JLabel label = new JLabel("Address: " + sipLayer.getHost() + ":" + String.valueOf(sipLayer.getPort()) + " Username:" + sipLayer.getUsername());
		grid.setConstraints(label, c);
		add(label);

		c.gridwidth = 2;
		c.gridy = 1;
		c.gridx = 0;
		register = new JButton("Register");
		register.addActionListener(this);
		register.setEnabled(false);
		grid.setConstraints(register, c);
		add(register);

		c.gridx = 2;
		bye = new JButton("Bye");
		bye.addActionListener(this);
		grid.setConstraints(bye, c);
		add(bye);

		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 2;
		label = new JLabel("dest:");
		grid.setConstraints(label, c);
		add(label);

		c.gridx = 1;
		c.weightx = 1;
		dest = new JTextField("sip:wilma@tiserver03.cpt.haw-hamburg.de");
		grid.setConstraints(dest, c);
		add(dest);

		c.gridx = 2;
		c.weightx = 0;
		call = new JButton("Call");
		call.addActionListener(this);
		grid.setConstraints(call, c);
		add(call);

		c.gridx = 3;
		cancel = new JButton("Cancel");
		cancel.setEnabled(false);
		cancel.addActionListener(this);
		grid.setConstraints(cancel, c);
		add(cancel);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 4;
		c.weighty = 1;
		c.weightx = 1;
		log = new JTextPane();
		logdoc = log.getStyledDocument();
		JScrollPane logscroll = new JScrollPane(log);
		logscroll.setPreferredSize(new Dimension(300, 400));
		logscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		grid.setConstraints(logscroll, c);
		add(logscroll);
	
		c.weighty = 0;


		pack();
		setVisible(true);
	}

	private void appendToLog(String message){
		try{
			logdoc.insertString(logdoc.getLength(), logTimeFormat.format(new Date()) + " " +  message + "\n", null); 
			log.setCaretPosition(logdoc.getLength());
		} catch(BadLocationException ex){
			ex.printStackTrace();
		}
	}

// -------------- MessageProcessor

	private ArrayList<String> buddies = new ArrayList<String>();

	public void processInvite(String sender){
	  appendToLog("Invite from " + sender);
	  if(!buddies.contains(sender)){
	    buddies.add(sender);
	    // TODO: if UAS -> trigger sending messages to all buddies
      igmp.beginSending();
	  }
	}

	public void processBye(String sender){
	  appendToLog("Bye from " + sender);
	  if(buddies.contains(sender)){
	    buddies.remove(sender);

      if(buddies.size() == 0)
        igmp.stopSending();

	    // TODO: if UAS -> check number of buddies and stop sending messages if count==0
	  }
	}

	public void processResponse(int statusCode){
		appendToLog("RESP: " + statusCode);
	}

	public void processMessage(String sender, String message){
		appendToLog("msg: " + message);
	}

public void processError(String errorMessage){
		appendToLog("err: " + errorMessage); 
	}

	public void processInfo(String infoMessage){
		appendToLog("info: " + infoMessage);
	}	

// --------------- ActionListener
	public void actionPerformed(ActionEvent e){
		Object source = e.getSource();
		if(source==register){
			try{
				sipLayer.register();
				bye.setEnabled(true);
				register.setEnabled(false);
			}catch(Exception ex){
				appendToLog(ex.getMessage());
			}
		}
		if(source==bye){
			try{
				sipLayer.bye();
				bye.setEnabled(false);
				register.setEnabled(true);
			}catch(Exception ex){
				appendToLog(ex.getMessage());
			}
		}
		if(source==call){
			try{
				inviteTransaction = sipLayer.invite(dest.getText());
				call.setEnabled(false);
				cancel.setEnabled(true);
				igmp.join();
			}catch(Exception ex){
				appendToLog(ex.getMessage());
			}
		}
		if(source==cancel){
			try{
				sipLayer.cancel(inviteTransaction);
				cancel.setEnabled(false);
				call.setEnabled(true);
				igmp.leave();
			}catch(Exception ex){
				appendToLog(ex.getMessage());
			}
		}
	}
}
