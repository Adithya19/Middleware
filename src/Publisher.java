import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Publisher {
    String name;
    PrintWriter writer;
    public Publisher (PrintWriter writer, String name) {
        this.writer = writer;
        this.name = name;
    }

    public void publish(String channel, String message){
        // send the message to the channel and tell the middleware the channel you want to publish to.
        writer.println(channel + ", " + message);
    }

}
