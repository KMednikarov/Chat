package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import static chatserver.ChatServer.welcomeMessage;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConnectionHandler extends Thread{
    private ServerDispatcher serverDispatcher;
    private ServerSocket serverSocket;
    private Socket socket;
    private Client client;
    public ConnectionHandler(Socket s,ServerSocket ss, ServerDispatcher sd){
        serverDispatcher = sd;
        serverSocket = ss;
        socket = s;
    }
    public synchronized void run(){
            try { 
                client = new Client();
                
                client.setSocket(socket);
                
                ClientListener clientListener = new ClientListener(client, serverDispatcher);
                ClientSender clientSender = new ClientSender(client, serverDispatcher);
                CommandInterpreter cmd = new CommandInterpreter(serverDispatcher);

                client.setCMD(cmd);
                client.setListener(clientListener);
                
                client.setSender(clientSender); 
                client.getSender().start();
                
                String name = "";
                //Disconnect the client if he quit 
                //before entering his name.
                try{
                    name = newNickname(client);  
                }catch(NullPointerException ex){
                    try{
                        disconnectUser();
                    }catch(InterruptedException iex){}
                    this.interrupt();                    
                    return;
                }
                
                client.setName(name); 
                 
                System.out.println(client.getName()+" has connected to the server.");                
                        
                serverDispatcher.addClient(client); 
                client.getSender().sendMessage( welcomeMessage); 
                clientListener.start();       
                System.out.println("Connected users: " 
                        + serverDispatcher.getAllClients().size() +"/"
                        + serverDispatcher.maxConnections);
                notify();
                this.interrupt();
                ChatServer.connectionAccepted(this);
            } catch (IOException ex) {
            }
}
    
    private String newNickname(Client client) throws IOException{
        String identify = "Please use [:meet <name>] to identify yourself:";
        client.getSender().sendMessage(identify);
        
        String name = client.getListener().getMessage();         
        name = client.getCMD().meetCommand(name); 
        while(true)
        {            
            if(!name.matches("[A-Za-z0-9]*"))
            {
                client.getSender()
                        .sendMessage("Please do not use special characters for your nickname.");
                
                client.getSender().sendMessage(identify);
                name = client.getListener().getMessage();
                name = client.getCMD().meetCommand(name);
                continue;
            }
            if(isFree(name) == false)
            {
                client.getSender()
                        .sendMessage("This nickname is already used.");
                client.getSender()
                        .sendMessage(identify);
                name = client.getListener().getMessage();
                name = client.getCMD().meetCommand(name);
                continue;
            }
            if(name.trim().equals(""))
            {
                client.getSender().sendMessage(identify);
                name = client.getListener().getMessage();
                name = client.getCMD().meetCommand(name);
                continue;
            }
            if(!normalNameLength(name)){
                client.getSender()
                        .sendMessage("Your nickname must be between 1 and 15 characters long.");
                
                client.getSender().sendMessage(identify);
                name = client.getListener().getMessage();
                name = client.getCMD().meetCommand(name);
                continue;
            }            
            break;            
        }
        return name;
    } 
    
    private boolean normalNameLength(String name){
            if(name.length()>=1 && name.length()<=15)
                return true;            
            else return false;
    }
    /**
     * Checks if the passed name is free(not connected).
     */
    private boolean isFree(String name){
        List<Client> connectedClients = serverDispatcher.getAllClients();
        for (Client x : connectedClients){
                if(name.toLowerCase()
                        .equals(x.getName().toLowerCase()))
                {                    
                    return false;
                }
            }
        return true;
    }
    
    public void disconnectUser() throws IOException, InterruptedException{
        if(client!=null){
            client.getSender().sendMessage("\nYou have been disconnected.");
            Thread.sleep(1);
            client.getSender().interrupt();
            client.getListener().interrupt();
            client.getSocket().close();     
        }
    }   
}