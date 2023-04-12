package Server;
import java.io.IOException;
import java.net.*;
import java.util.*;

public class Dispatcher {
    private static ServerSocket port;
    private static int count = 0;

    public static void main(String[] args){
        ServerThread thread;
        Socket client;

        try{
            port = new ServerSocket(9877);

            while(true){
                client = port.accept();
                thread = new ServerThread(client, ++count);
                thread.start();
            }
        }
        catch(IOException ex){
            System.out.println("IOException on socket: " + ex);
            ex.printStackTrace();
        }
    }
}
