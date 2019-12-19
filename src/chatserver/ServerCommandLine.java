package chatserver;

import java.io.IOException;
import java.util.Scanner;

/**
 * This class is used to accept and handle some
 * server commands.
 */
public class ServerCommandLine extends Thread{
    private String commandLine;
    
    public void run(){
       while(true){
        readCommand();
        if(commandLine.equals(":quit")) 
            System.exit(0);
        else if(commandLine.startsWith("-m "))
            changeWelcomeMessage();
        else if(commandLine.startsWith("-n "))
            setMaxConnections();
        else if(commandLine.startsWith("-p "))
            try {
                changePort();
            } 
            catch (InterruptedException ex) {
            }        
        else System.err.println("Unkown command.");
       }
    }
    
    private void changeWelcomeMessage(){
        String tmp = commandLine;
        
        tmp = tmp.substring(tmp.indexOf(" "));
        tmp = tmp.trim();
        
        ChatServer.welcomeMessage = tmp;
        System.out.println("Welcome message changed to: \""+tmp+"\".");
        clearCommandLine();
    }
    
    private synchronized void setMaxConnections() {
        String tmp = commandLine;
        
        tmp = tmp.substring(tmp.indexOf(" "));
        tmp = tmp.trim();
        
        try{            
            ServerDispatcher.maxConnections = Integer.parseInt(tmp);
            System.out.printf("You have successfully changed"
                    +" the maximum connections to %s.\n", ServerDispatcher.maxConnections);
        } catch(NumberFormatException ex){
            System.err.println("Please enter a number.");
        }
        
        notify();
        clearCommandLine();
    }
    
    private void changePort() throws InterruptedException {
        try{
            int newPort = Integer.parseInt(commandLine
                    .substring(commandLine.indexOf(" ")).trim());
            if(newPort<1 || newPort>65535)
                {                    
                    throw new InvalidPortNumberException();
                }
            if(newPort == ChatServer.serverPort)
            {
                System.err.println("The server is already listening that port. ["+newPort+"].");
                return;
            }
            ChatServer.serverPort = newPort;
            ChatServer.changeServerSocket(newPort);            
        }catch(NumberFormatException ex){
            System.err.println("Invalid number.");
        }catch(IOException ex){
            System.err.println("An I/O error has occurred while trying to change the port.");
        }catch(InvalidPortNumberException ex){
            System.err.println("Port number must be between 1 and 65535.");
        }
        clearCommandLine();
    } 
    
    public String readCommand(){
        Scanner scanner = new Scanner(System.in);
        commandLine = scanner.nextLine().trim();        
        return commandLine;
    }
    public String getCommand(){
        return commandLine;
    }
    private void clearCommandLine(){        
        commandLine = "";
    }
}