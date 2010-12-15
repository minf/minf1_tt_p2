package dev2dev.textclient;

public interface MessageProcessor
{
    public void processInvite(String sender);
    public void processBye(String sender);
    public void processMessage(String sender, String message);
    public void processError(String errorMessage);
    public void processInfo(String infoMessage);
}
