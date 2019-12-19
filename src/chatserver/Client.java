package chatserver;

import java.net.Socket;

public class Client {
    private String clientName;
    private Socket clientSocket;
    private ClientListener clientListener;
    private ClientSender clientSender;
    private CommandInterpreter cmd;;
    
    public void setName(String name){
        clientName = name;
    }
    public String getName(){
        return clientName;
    }
    public void setSocket(Socket newSocket){
        clientSocket = newSocket;
    }
    public Socket getSocket(){
        return clientSocket;
    }    
    public void setListener(ClientListener newListener){
        clientListener = newListener;
    }
    public ClientListener getListener(){
        return clientListener;
    }    
    public void setSender(ClientSender newSender){
        clientSender = newSender;
    }
    public ClientSender getSender(){
        return clientSender;
    }    
    public void setCMD(CommandInterpreter newCMD){
        cmd = newCMD;
    }
    public CommandInterpreter getCMD(){
        return cmd;
    }
}