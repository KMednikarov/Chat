package chatserver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/** 
 * Sends messages to the client. Messages waiting to be sent 
 * are stored in a message queue. When the queue is empty, 
 * ClientSender falls in sleep until a new message is arrived 
 * in the queue. When the queue is not empty, ClientSender 
 * sends the messages from the queue to the client socket. 
 */ 
public class ClientSender extends Thread{
    private List messageQueue;
    private ServerDispatcher serverDispatcher;
    private Client client;
    private PrintWriter socketWriter;
    
    public ClientSender(Client newClient, ServerDispatcher newDispatcher) throws IOException{
        client = newClient;
        serverDispatcher = newDispatcher;
        messageQueue = new ArrayList<String>();
        socketWriter = new PrintWriter( 
            new OutputStreamWriter(client.getSocket().getOutputStream())); 
    }    
    /** 
     * Until interrupted, reads messages from the message queue 
     * and sends them to the client's socket. 
     */ 
    public void run(){
        try { 
            while (!isInterrupted()) { 
                String message = getNextMessageFromQueue(); 
                sendMessageToClient(message); 
            } 
        } catch (Exception ex) { 
            ex.printStackTrace();
        } 
        // Communication is broken. Interrupt listener thread.
        client.getListener().interrupt(); 
        try { 
            serverDispatcher.disconnectClient(client);
        } catch (IOException ex) {
        }
    }    
    /** 
     * Adds given message to the message queue and notifies 
     * this thread (actually getNextMessageFromQueue method) 
     * that a message is arrived. sendMessage is always called 
     * by other threads (ServerDispatcher). 
     * @param newMessage
     */ 
    public synchronized void sendMessage(String newMessage){
        messageQueue.add(newMessage); 
        notify(); 
    }    
    
    /** 
     * @return and deletes the next message from the message 
     * queue. If the queue is empty, falls in sleep until 
     * notified for message arrival by sendMessage method. 
     */ 
    private synchronized String getNextMessageFromQueue() 
            throws InterruptedException { 
        if(this.isInterrupted()) return "";
        while (messageQueue.isEmpty()) 
        {
            try{
                wait(); 
            }
            catch(InterruptedException ex){
            }
        }
        String message = (String) messageQueue.get(0);         
        messageQueue.remove(0); 
        return message;
    } 
    
    private void sendMessageToClient(String message){
        socketWriter.println(message);
        socketWriter.flush();
    }
}