import java.net.Socket;

public class Subscriber {
    String channel;
    Socket socket;

    public Subscriber(Socket socket){
        this.socket = socket;
    }

    public void subscribe(String channelName){
        Channel channel = Middleware.channelHashMap.get(channelName);
        channel.subscribe(socket);
    }
}
