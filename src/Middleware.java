import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class Middleware {
    static Socket clientSocket;
    static ArrayList<Socket> clientSockets = new ArrayList<>();
    static HashMap<String, Channel> channelHashMap = new HashMap<>();
    static ArrayList<String> publishers = new ArrayList<>();
    public static void main(String[] args) throws IOException {

        Channel newChannel = new Channel("firstchannel");
        ServerSocket ss = new ServerSocket(3000);
        while(true){
            System.out.println("Server waiting for connections");
            clientSocket = ss.accept();
            clientSockets.add(clientSocket);
            Thread t1 = new Thread(new ClientHandlerThread(clientSocket));
            t1.start();
        }
    }
}
