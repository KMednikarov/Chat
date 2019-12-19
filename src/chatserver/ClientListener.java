package chatserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** 
 * ClientListener class listens for client messages and 
 * forwards them to ServerDispatcher. 
 */ 
public class ClientListener extends Thread{
    private ServerDispatcher serverDispatcher;
    private CommandInterpreter cmd;
    private Client client;
    private BufferedReader socketReader;
    private final String COMMAND_SYMBOL = ":";
    private String clientMessage = "";
    
    public ClientListener(Client newClient, ServerDispatcher newDispatcher) throws IOException{
        client = newClient;
        serverDispatcher = newDispatcher;
        cmd = new CommandInterpreter(newDispatcher);
        
        socketReader = new BufferedReader(
                new InputStreamReader(client.getSocket().getInputStream()));
    }
    
    /** 
     * Until interrupted, reads messages from the client 
     * socket, forwards them to the server dispatcher's 
     * queue and notifies the server dispatcher. 
     */ 
    public void run(){
        try { 
            while (!isInterrupted()) { 
                clientMessage = getMessage();
                clientMessage = clientMessage.trim();
                if (clientMessage == null) 
                    break; 
                if(clientMessage.equals(""))
                    continue;
                if(clientMessage.startsWith(COMMAND_SYMBOL)){                    
                    cmd.executeCommand(client, clientMessage);
                    continue;
                }
                serverDispatcher.dispatchMessage(client, clientMessage); 
            } 
        } catch (IOException ex) { 
           
        } catch(NullPointerException ex){
            
        }
        // Communication is broken. Interrupt 
        // sender thread. 
        client.getSender().interrupt(); 
        try { 
            serverDispatcher.disconnectClient(client);
        } catch (IOException ex) {            
        }
    }
    
    public String getMessage() throws IOException{   
        String message = "";
        message = socketReader.readLine();
        return message;
    }
}