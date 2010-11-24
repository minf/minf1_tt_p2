package sipPeer;

import java.net.*;
import java.util.*;
//import javax.sip.*;
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
			System.out.println("usage: java -jar SipPeer.jar username port");
			System.exit(0);
		}
		try{
			String username = args[0];
			int port = Integer.parseInt(args[1]);
			String ip = InetAddress.getLocalHost().getHostAddress();

			SipLayer sipLayer = new SipLayer(username, ip, port);
			
			SipPeer app = new SipPeer(sipLayer);
			sipLayer.register();
			app.show();
		} catch(Exception ex){
			ex.printStackTrace();
		}		
	}

	private SipLayer sipLayer;
	private JTextField username;
	private JButton register,bye;
	private JTextPane log;
	private StyledDocument logdoc;

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

		initUI();
	}

	private void initUI(){
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.gridwidth = c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = c.ipady = 4;

		setLayout(grid);

		c.gridwidth=3;
		JLabel label = new JLabel("Address: " + sipLayer.getHost() + ":" + String.valueOf(sipLayer.getPort()));
		grid.setConstraints(label, c);
		add(label);

		c.gridwidth = 1;
		c.gridy = 1;
		c.gridx = 0;
		label = new JLabel("username:");
		grid.setConstraints(label, c);
		add(label);

		c.gridx = 1;
		username = new JTextField(sipLayer.getUsername());
		grid.setConstraints(username, c);
		add(username);

		c.gridx = 2;
		register = new JButton("Register");
		register.addActionListener(this);
		register.setEnabled(false);
		grid.setConstraints(register, c);
		add(register);

		c.gridx = 3;
		bye = new JButton("Bye");
		bye.addActionListener(this);
		grid.setConstraints(bye, c);
		add(bye);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 4;
		c.weighty = 1;
		log = new JTextPane();
		logdoc = log.getStyledDocument();
		JScrollPane logscroll = new JScrollPane(log);
		logscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		grid.setConstraints(logscroll, c);
		add(logscroll);
	
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;


		pack();
		setVisible(true);
	}

	private void appendToLog(String message){
		try{
			logdoc.insertString(logdoc.getLength(), "msg: " + message + "\n", null); 
		} catch(BadLocationException ex){
			ex.printStackTrace();
		}
	}

// -------------- MessageProcessor
	public void processMessage(String sender, String message){
		appendToLog("msg: " + message + "\n");
	}

	public void processError(String errorMessage){
		appendToLog("err: " + errorMessage + "\n"); 
	}

	public void processInfo(String infoMessage){
		appendToLog("info: " + infoMessage + "\n");
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
		//sipLayer.setUsername(username.getText());
			
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
	}

}
