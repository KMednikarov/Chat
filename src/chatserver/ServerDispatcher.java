package chatserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** 
 * ServerDispatcher class is purposed to listen for messages 
 * received from the clients and to dispatch them to all the 
 * clients connected to the chat server. 
 */ 
public class ServerDispatcher extends Thread{
    public static int maxConnections=50;
    private List<String> messageQueue = new ArrayList<String>();
    private List<Client> clientsList = new ArrayList<Client>();
    private Client messageSender;
    private static final String DISCONNECT_MESSAGE = "has disconnected from server.";
    
    /** 
     * Infinitely reads messages from the queue and dispatches 
     * them to all clients connected to the server. 
     */ 
    public void run(){        
        try { 
            while (true) { 
                String message = getNextMessageFromQueue(); 
                sendMessageToAllClients(message); 
            } 
        } catch (InterruptedException ex) { 
            System.err.println("Dispatcher has been interrupted.");
        } 
    }
    /** 
     * If the connected clients are less
     * than the maximum connections number and
     * adds given client to the server's clients list. 
     */ 
    public synchronized void addClient(Client newClient){       
        if(clientsList.size() >= maxConnections) {
            try {                
                newClient.getSender().sendMessage("\nThere are too many connected users."
                                                    +"Please wait for free place.");
                Thread.sleep(5);
                newClient.getListener().interrupt();
                newClient.getSender().interrupt();
                newClient.getSocket().setReuseAddress(true);
                newClient.getSocket().close();
                if(newClient.getName()!="!@"){
                    System.err.println(newClient.getName()
                            +" has been disconnected"
                            +" because there are too many"
                            +" connected users to the server: " 
                            +clientsList.size()+"/"+maxConnections+".");
                }
                return;
            } catch (Exception ex) {
                Logger.getLogger(ServerDispatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        clientsList.add(newClient);          
    }
    
    public void disconnectClient(Client removeClient) throws IOException{
        int clientIndex = clientsList.indexOf(removeClient);
        if(clientIndex == -1) return;
        
        clientsList.remove(clientIndex);
        removeClient.getListener().interrupt();
        removeClient.getSender().interrupt();
        removeClient.getSocket().close();
        
        System.out.println(removeClient.getName()+" "+DISCONNECT_MESSAGE);
        System.out.println("Connected users: " 
                        + clientsList.size() +"/"
                        + maxConnections);
    }
    
    public void disconnectAllclients() throws IOException, InterruptedException{   
        sendBroadCastMessage(generateBroadcastMessage());
        for(int i=clientsList.size(); i>0;i--){
            Client x = clientsList.get(0);             
            clientsList.remove(x);
            
            Thread.sleep(5);
            
            x.getListener().interrupt();
            x.getSender().interrupt();   
            x.getSocket().setReuseAddress(true);         
            x.getSocket().close();
        }
    }
    
    /** 
     * Add message to the dispatcher's message queue and 
     * notifies this thread to wake up the message queue reader 
     * (getNextMessageFromQueue method). dispatchMessage method 
     * is called by other threads (ClientListener) when a 
     * message is arrived. 
     */ 
    public synchronized void dispatchMessage(Client client, String message){        
        messageSender = client;
        message = client.getName() + " : " + message; 
        messageQueue.add(message);   
        notify();
    }
    /** 
     * @return and deletes the next message from the message 
     * queue. If there is no messages in the queue, falls in 
     * sleep until notified by dispatchMessage method. 
     */ 
    private synchronized String getNextMessageFromQueue() throws InterruptedException { 
        while (messageQueue.isEmpty()) 
        {
            wait(); 
        }
        String message = messageQueue.get(0); 
        messageQueue.remove(0); 
        return message; 
    } 
    /** 
     * Send message from user to all other clients in the clients list. 
     * Actually the message is added to the client sender 
     * thread's message queue and this client sender thread 
     * is notified to process it. 
     */ 
    private void sendMessageToAllClients(String message){        
            String senderName = messageSender.getName();
            for (Client client : clientsList) { 
                String receiverName = client.getName();
                if(senderName.equals(receiverName))
                {
                    continue;
                }
                client.getSender().sendMessage(message); 
            }         
    }    
    
    public void sendBroadCastMessage(String message){
        for (Client client : clientsList) { 
                client.getSender().sendMessage(message); 
        }         
    }
    
    private String generateBroadcastMessage(){
        String broadcastMessage = "\nYou have been disconnected,"
                        +" because the server port was changed to "
                        +ChatServer.serverPort+".";
        return broadcastMessage;
    }   
    public List getAllClients(){
        return clientsList;
    }
    public void removeClient(Client x){
        clientsList.remove(x);
    }
}