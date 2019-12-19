package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/** 
 * ChatServer class is the entry point for the server. 
 * It opens a server socket, starts the dispatcher thread and 
 * infinitely accepts client connections, creates threads for 
 * handling them and starts these threads. 
 */ 
public class ChatServer {
    public static  String welcomeMessage = "Hello and welcome to our chat room.";    
    public static int serverPort = -1;
    private static final int DEFAULT_PORT = 3456;
    private static ServerCommandLine serverCMD;
    private static ServerSocket serverSocket;
    private static ServerDispatcher serverDispatcher;
    private static List<ConnectionHandler> connectionThread;
    
    public static void main(String[] args) throws IOException, InterruptedException { 
        serverCMD = new ServerCommandLine();        
        connectionThread = new ArrayList<ConnectionHandler>();
        while(true){
            System.out.print("Port number[0 for default port]: ");
            String tmp = serverCMD.readCommand();
            try{
                if(tmp.trim()
                        .equals("0"))
                {
                    serverPort = DEFAULT_PORT;
                    break;
                }
                serverPort = Integer.parseInt(tmp);
                if(serverPort<1 || serverPort>65535)
                {
                    System.err.println("Port number must be between 1 and 65535.");
                    continue;
                }
                break;
            }catch(NumberFormatException ex){
                System.err.println("Invalid number.");
            }
        }
        serverCMD.start();
        
        if(serverPort==-1){           
            serverPort = DEFAULT_PORT;
        }   
        // Start listening on the server socket 
        setServerSocket(serverPort);
        serverDispatcher = new ServerDispatcher();
        serverDispatcher.start();
        // Infinitely accept and handle client connections
        handleClientConnections();
    }
       
    /**
     * Waiting for a new client to connect.
     * When a client, new ClientListener and ClientSender
     * threads are started.
     * @throws InterruptedException 
     */
    private static void handleClientConnections() throws InterruptedException{  
       /**
        * If the socket is changed accepting
        * new connections is stopped.
        */        
        while(true){
            try {           
                Socket socket = serverSocket.accept();
                ConnectionHandler ch = new ConnectionHandler(socket, serverSocket, serverDispatcher);
                ch.start();
                connectionThread.add(ch);
            } catch (IOException ex) { } 
        }
    }
    
    /**
     * Change the server port.
     * The current server socket is closed after
     * all connected users are disconnected.
     * @throws IOException 
     */    
    public static void changeServerSocket(int port) throws IOException, InterruptedException{
        serverSocket.close(); 
        setServerSocket(port);   
        serverDispatcher.sendBroadCastMessage("Server port was changed to ["+serverPort+"].");
    }    
    private static void setServerSocket(int port){
        try {            
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
        } catch (IOException ex) {
            System.err.println("Cannot open port "+port);
            ex.printStackTrace();
            return;
        }
        System.out.println("Chat server successfully started on port " 
                + serverSocket.getLocalPort());        
    }
    public static void connectionAccepted(ConnectionHandler ch){
        connectionThread.remove(ch);
    }
}