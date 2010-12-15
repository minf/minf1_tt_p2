package dev2dev.textclient;

import java.net.InetAddress;
import java.text.ParseException;
import java.util.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

public class SipLayer implements SipListener {
  
  private MessageProcessor messageProcessor;
  
  private String username;
  
  private SipStack sipStack;
  
  private SipFactory sipFactory;
  
  private AddressFactory addressFactory;
  
  private HeaderFactory headerFactory;
  
  private MessageFactory messageFactory;
  
  private SipProvider sipProvider;
  
  private final String PROXY = "tiserver03.cpt.haw-hamburg.de";
  
  private CallIdHeader callIdHeader = null;
  
  private int cseqCounter = 0;
  
  private int nextCSeqId(){
    return ++cseqCounter;
  }
  
  /** Here we initialize the SIP stack. */
  public SipLayer(String username, String ip, int port)
  throws PeerUnavailableException, TransportNotSupportedException,
  InvalidArgumentException, ObjectInUseException,
  TooManyListenersException {
    setUsername(username);
    sipFactory = SipFactory.getInstance();
    sipFactory.setPathName("gov.nist");
    Properties properties = new Properties();
    properties.setProperty("javax.sip.STACK_NAME", "SipPeer");
    //properties.setProperty("javax.sip.IP_ADDRESS", ip);
    properties.setProperty("javax.sip.OUTBOUND_PROXY", PROXY + ":5060/" + ListeningPoint.UDP);
    
    //DEBUGGING: Information will go to files 
    //textclient.log and textclientdebug.log
    properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
    properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
			   "sipPeer_server_log.txt");
    properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
			   "sipPeer_debug.txt");
    
    sipStack = sipFactory.createSipStack(properties);
    headerFactory = sipFactory.createHeaderFactory();
    addressFactory = sipFactory.createAddressFactory();
    messageFactory = sipFactory.createMessageFactory();
    
    ListeningPoint lp = sipStack.createListeningPoint(ip, port, ListeningPoint.UDP);
    
    sipProvider = sipStack.createSipProvider(lp);
    sipProvider.addSipListener(this);
  }
  
  private FromHeader getFromHeader() throws ParseException{
    SipURI from = addressFactory.createSipURI(getUsername(), PROXY);
    Address fromNameAddress = addressFactory.createAddress(from);
    //fromNameAddress.setDisplayName(getUsername());
    return headerFactory.createFromHeader(fromNameAddress, "sippeerv1.0");
  }
  
  
  private ToHeader getToHeader(String to) throws ParseException{
    String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
    String address = to.substring(to.indexOf("@") + 1);
    return getToHeader(username, address);
  }
  
  private ToHeader getToHeader(String username, String address) throws ParseException{
    SipURI toAddress = addressFactory.createSipURI(username, address);
    Address toNameAddress = addressFactory.createAddress(toAddress);
    //toNameAddress.setDisplayName(username);
    return headerFactory.createToHeader(toNameAddress, null);
  }
  
  
  public void register() throws ParseException, InvalidArgumentException, SipException{
    FromHeader fromHeader = getFromHeader();
    ToHeader toHeader = getToHeader("sip:"+username+"@" + PROXY);
    
    URI requestURI = addressFactory.createURI("sip:" + PROXY);
    
    callIdHeader = sipProvider.getNewCallId();
    CSeqHeader cSeq = headerFactory.createCSeqHeader(nextCSeqId(), Request.REGISTER);
    
    List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
    viaHeaders.add(headerFactory.createViaHeader(getHost(), getPort(), getTransport(), "branch13423432947329047320974320974230947298;rport"));
    
    MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
    
    Request request = messageFactory.createRequest(requestURI, Request.REGISTER, callIdHeader, cSeq, fromHeader, toHeader, viaHeaders, maxForwards);
    
    SipURI contactURI = addressFactory.createSipURI(username, getHost());
    Address contactAddress = addressFactory.createAddress(contactURI);
    ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
    
    request.addHeader(contactHeader);
    
    sipProvider.sendRequest(request);
  }
  
  public Dialog invite(String dest) throws ParseException, InvalidArgumentException, SipException, TransactionUnavailableException{
    FromHeader fromHeader = getFromHeader();
    ToHeader toHeader = getToHeader(dest);
    
    URI requestURI = addressFactory.createURI(dest);
    
    CallIdHeader inviteCallIdHeader = sipProvider.getNewCallId();
    CSeqHeader cSeq = headerFactory.createCSeqHeader(nextCSeqId(), Request.INVITE);
    
    List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
    viaHeaders.add(headerFactory.createViaHeader(getHost(), getPort(), getTransport(), "branch13423432947329047320974320974230947298;rport"));
    
    MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
    
    Request request = messageFactory.createRequest(requestURI, Request.INVITE, inviteCallIdHeader, cSeq, fromHeader, toHeader, viaHeaders, maxForwards);
    
    SipURI contactURI = addressFactory.createSipURI(username, getHost());
    Address contactAddress = addressFactory.createAddress(contactURI);
    ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
    
    request.addHeader(contactHeader);
    
    ClientTransaction t = sipProvider.getNewClientTransaction(request);
    t.sendRequest();
    return t.getDialog();
  }  

  public void hangup(Dialog dialog) throws ParseException, InvalidArgumentException, SipException, TransactionUnavailableException{
     if(dialog == null) return;

     Request byeRequest = dialog.createRequest(Request.BYE);

     ClientTransaction t = sipProvider.getNewClientTransaction(byeRequest);

     dialog.sendRequest(t);
  }  

  public void cancel(ClientTransaction ctransaction) throws SipException, TransactionUnavailableException{
    Request cancelRequest = ctransaction.createCancel();
    ClientTransaction t = sipProvider.getNewClientTransaction(cancelRequest);
    t.sendRequest();
  }
  
  /*public void cancel(String dest, CallIdHeader inviteCallIdHeader) throws ParseException, InvalidArgumentException, SipException{
   *        FromHeader fromHeader = getFromHeader();
   *	ToHeader toHeader = getToHeader(dest);
   *	
   *	URI requestURI = addressFactory.createURI(dest);
   *	
   *	CSeqHeader cSeq = headerFactory.createCSeqHeader(nextCSeqId(), Request.CANCEL);
   * 
   *	List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
   *	viaHeaders.add(headerFactory.createViaHeader(getHost(), getPort(), getTransport(), "branch13423432947329047320974320974230947298;rport"));
   * 
   *	MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
   * 
   *	Request request = messageFactory.createRequest(requestURI, Request.CANCEL, inviteCallIdHeader, cSeq, fromHeader, toHeader, viaHeaders, maxForwards);
   * 
   *	SipURI contactURI = addressFactory.createSipURI(username, getHost());
   *	Address contactAddress = addressFactory.createAddress(contactURI);
   *	ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
   * 
   *	request.addHeader(contactHeader);
   *	
   *	sipProvider.sendRequest(request);
   *}
   */
  
  public void bye() throws ParseException, InvalidArgumentException, SipException {
    FromHeader fromHeader = getFromHeader();
    ToHeader toHeader = getToHeader("sip:"+username+"@" + PROXY);
    
    URI requestURI = addressFactory.createURI("sip:" + PROXY);
    
    //CallIdHeader callIdHeader = sipProvider.getNewCallId();
    CSeqHeader cSeq = headerFactory.createCSeqHeader(nextCSeqId(), Request.REGISTER);
    
    List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
    viaHeaders.add(headerFactory.createViaHeader(getHost(), getPort(), getTransport(), "branch13423432947329047320974320974230947298;rport"));
    
    MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);
    
    Request request = messageFactory.createRequest(requestURI, Request.REGISTER, callIdHeader, cSeq, fromHeader, toHeader, viaHeaders, maxForwards);
    
    ContactHeader contactHeader = headerFactory.createContactHeader();
    request.addHeader(contactHeader);
    
    ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(0);
    request.addHeader(expiresHeader);
    
    sipProvider.sendRequest(request);
    
    callIdHeader = null;
  }
  
  
  
  
  /**
   * This method uses the SIP stack to send a message. 
   */
  public void sendMessage(String to, String message) throws ParseException,
  InvalidArgumentException, SipException {
    String username = to.substring(to.indexOf(":") + 1, to.indexOf("@"));
    String address = to.substring(to.indexOf("@") + 1);
    
    FromHeader fromHeader = getFromHeader();
    ToHeader toHeader = getToHeader(username, address);
    
    SipURI requestURI = addressFactory.createSipURI(username, address);
    requestURI.setTransportParam("udp");
    
    ArrayList viaHeaders = new ArrayList();
    ViaHeader viaHeader = headerFactory.createViaHeader(getHost(),
							getPort(), "udp", "branch1");
    viaHeaders.add(viaHeader);
    
    CallIdHeader callIdHeader = sipProvider.getNewCallId();
    
    CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1,
							   Request.MESSAGE);
    
    MaxForwardsHeader maxForwards = headerFactory
    .createMaxForwardsHeader(70);
    
    Request request = messageFactory.createRequest(requestURI,
						   Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
						   toHeader, viaHeaders, maxForwards);
    
    SipURI contactURI = addressFactory.createSipURI(getUsername(),
						    getHost());
    contactURI.setPort(getPort());
    Address contactAddress = addressFactory.createAddress(contactURI);
    contactAddress.setDisplayName(getUsername());
    ContactHeader contactHeader = headerFactory
    .createContactHeader(contactAddress);
    request.addHeader(contactHeader);
    
    ContentTypeHeader contentTypeHeader = headerFactory
    .createContentTypeHeader("text", "plain");
    request.setContent(message, contentTypeHeader);
    
    sipProvider.sendRequest(request);
  }
  
  /** This method is called by the SIP stack when a response arrives. */
  public void processResponse(ResponseEvent evt) {
    Response response = evt.getResponse();
    int status = response.getStatusCode();
    // TODO brauchen wir das? messageProcessor.processResponse(status);
    
    ClientTransaction transaction = evt.getClientTransaction();
    Dialog dialog = evt.getDialog();
    
    if ((status >= 200) && (status < 300)) { //Success!
      messageProcessor.processInfo("--Sent " + response.getReasonPhrase());
      
      if(transaction != null) {
        try {
          CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME); 
          Request ackRequest = dialog.createAck(cseq.getSeqNumber()); 
          dialog.sendAck(ackRequest);
        } catch(Exception ex) {
          messageProcessor.processError(ex.getMessage());
        }
      }

      return;
    } else if(status == 487 /*REQUEST_TERMINATED*/){
      messageProcessor.processInfo("-- RequestTerminated");

      return;
    }
    
    messageProcessor.processError("Previous message not sent: " + status);
  }
  
  /** 
   * This method is called by the SIP stack when a new request arrives. 
   */
  public void processRequest(RequestEvent evt) {
    Request req = evt.getRequest();
    
    String method = req.getMethod();
    /*if (!method.equals("MESSAGE") && !method.equals("INVITE") { //bad request type.
     *	    messageProcessor.processError("Bad request type: " + method);
     *	    return;
     *}
     */
    
    FromHeader from = (FromHeader) req.getHeader("From");
    
    if(method.equals("MESSAGE")){
      messageProcessor.processMessage(from.getAddress().toString(), new String(req.getRawContent()));
    }else if(method.equals("INVITE")){
      messageProcessor.processInvite(from.getAddress().toString());
    }else if(method.equals("BYE")){
      messageProcessor.processBye(from.getAddress().toString());
    }else if(method.equals("ACK")) {
      messageProcessor.processInfo("Ack received");
      return;
    } else{
      messageProcessor.processError("Bad request type: " + method + " from: " + from.getAddress().toString());
      return;
    }

    Response response = null;
    try { //Reply with OK
    response = messageFactory.createResponse(200, req);
    ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
    toHeader.setTag("888"); //This is mandatory as per the spec.

    SipURI contactURI = addressFactory.createSipURI(username, getHost());
    Address contactAddress = addressFactory.createAddress(contactURI);
    ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
    
    response.addHeader(contactHeader);
 
    ServerTransaction st = sipProvider.getNewServerTransaction(req);
    st.sendResponse(response);
    } catch (Throwable e) {
      e.printStackTrace();
      messageProcessor.processError("Can't send OK reply.");
    }
    }
    
    /** 
     * This method is called by the SIP stack when there's no answer 
     * to a message. Note that this is treated differently from an error
     * message. 
     */
    public void processTimeout(TimeoutEvent evt) {
      messageProcessor
      .processError("Previous message not sent: " + "timeout");
    }
    
    /** 
     * This method is called by the SIP stack when there's an asynchronous
     * message transmission error.  
     */
    public void processIOException(IOExceptionEvent evt) {
      messageProcessor.processError("Previous message not sent: "
				    + "I/O Exception");
    }
    
    /** 
     * This method is called by the SIP stack when a dialog (session) ends. 
     */
    public void processDialogTerminated(DialogTerminatedEvent evt) {
    }
    
    /** 
     * This method is called by the SIP stack when a transaction ends. 
     */
    public void processTransactionTerminated(TransactionTerminatedEvent evt) {
    }
    
    public String getHost() {
      //int port = sipProvider.getListeningPoint().getPort();
      //String host = sipStack.getIPAddress();
      return sipProvider.getListeningPoint().getIPAddress();
    }
    
    public int getPort() {
      return sipProvider.getListeningPoint().getPort();
    }
    
    public String getTransport() {
      return sipProvider.getListeningPoint().getTransport();
    }
    
    public String getUsername() {
      return username;
    }
    
    public void setUsername(String newUsername) {
      username = newUsername;
    }
    
    public MessageProcessor getMessageProcessor() {
      return messageProcessor;
    }
    
    public void setMessageProcessor(MessageProcessor newMessageProcessor) {
      messageProcessor = newMessageProcessor;
    }
    
    }
    
