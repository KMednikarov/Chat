package chatserver;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CommandInterpreter {
    private ServerDispatcher serverDispatcher;
    private String execCommand="";
    private String parameters="";
    
    public CommandInterpreter(ServerDispatcher newDispatcher){
        serverDispatcher = newDispatcher;
    }
    
    public void executeCommand(Client client, String message){
        //Quit command
        if(message.toLowerCase().trim()
                .equals(":quit")){ 
            quitCommand(client, message);
        }        
        //Who command
        else if(message.toLowerCase().trim()
                .equals(":who"))
        {
            whoCommand(client, message);
        }
        //Whisper command
        else if(message.toLowerCase().trim()
                .startsWith(":whisper")){
            whisperCommand(client, message);
        }
        //Meet command
        else if(message.toLowerCase().trim()
                .startsWith(":meet "))
        {
            meetCommand(message);
        }
        else client.getSender().sendMessage("Unkown or wrong command");
    } 
    public void quitCommand(Client client, String message){
        if(message.toLowerCase().trim()
                .equals(":quit")){ 
            try {     
                serverDispatcher.disconnectClient(client);
            } catch (IOException ex) {
                Logger.getLogger(CommandInterpreter.class.getName()).log(Level.SEVERE, null, ex);
            }
            client.getListener().interrupt();
            client.getSender().interrupt();
            try {
                client.getSocket().close();
            } catch (IOException ex) {
                Logger.getLogger(CommandInterpreter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }            
    }
    public void whisperCommand(Client client, String message){
        List<Client> connectedClients = serverDispatcher.getAllClients();
        if(!message.trim().contains(",")
                || !(message.toLowerCase().trim()
                        .startsWith(":whisper ")))
        {
            client.getSender()
                    .sendMessage("Wrong command. Please use [:whisper <name>, <message>].");
            return;
        }
        separate_CommandParameters(message);
        
        String recipientName = parameters.substring(0, parameters.indexOf(","));
        String whisperMessage = parameters
                                .substring(parameters.indexOf(",")+1
                                        , parameters.length());
        if(recipientName.trim().equals("")){
            client.getSender()
                    .sendMessage("Wrong command. Please use [:whisper <name>, <message>].");
            return;
        }
        String senderName = client.getName();
        boolean userNotFound=true;
        for (Client x : connectedClients){
            if(recipientName.toLowerCase().trim()
                    .equals(x.getName().toLowerCase()))
            {
                whisperMessage = "- "+senderName + "(whisper): "+whisperMessage;
                x.getSender().sendMessage(whisperMessage);
                userNotFound=false;
                break;
            }
        }            
        if(userNotFound){
            client.getSender().sendMessage("There is no such user.");
        }
        parameters = "";        
    }
    
    public void whoCommand(Client client, String message){
        if(message.toLowerCase().trim()
                .equals(":who")){
            List<Client> connectedClients = serverDispatcher.getAllClients();
            int clientsCounter = 1;
            client.getSender().sendMessage("\nAll connected clients:");
            for(Client x : connectedClients){
                if(x.getName().equalsIgnoreCase("!@")) continue;
                client.getSender()
                        .sendMessage(clientsCounter+". "+x.getName());
                clientsCounter++;
            }            
            client.getSender().sendMessage("--- End of list ---\n");
        }
    }
    
    public String meetCommand(String message){
        String name = "";
        if(message.toLowerCase().trim()
                .startsWith(":meet ")){
            separate_CommandParameters(message);            
            name = parameters;            
            parameters = "";
        }
        return name;
    }
    /**
     * If there are parameters entered by the user
     * they are separated from the command.
     * @param message 
     */
    private void separate_CommandParameters(String message){
        if(message.toLowerCase()
                .contains(" ")){
            execCommand = message.substring(0 , message.indexOf(" "));
            parameters = message.substring(message.indexOf(" ")+1 , message.length());
        }
    }
}