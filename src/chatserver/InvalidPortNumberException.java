package chatserver;

/**
 * Custom exception to handle too big port numbers.
 * It is used in the ServerCommandLine -> changePort() function.
 */
public class InvalidPortNumberException extends Exception{
    public InvalidPortNumberException(){
        super();
    }
}