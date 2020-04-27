import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ClientHandlerThread implements Runnable{
    BufferedReader reader;
    Socket socket;
    PrintWriter pw;
    String messages[];

    public ClientHandlerThread(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        System.out.println("Created thread 1");
        String input;
        try {
            this.pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("Hello from the server");
            while(true){
                input = reader.readLine();
                if(input == null)
                    return;
                if(input.equals("publisher")){
                    System.out.println("The client chose to be a publisher");
                    String pubName = reader.readLine();
                    // check for the name in the hashmap
                    if(Middleware.publishers.contains(pubName))
                        pw.println("Welcome back, " + pubName);
                    else{
                        Middleware.publishers.add(pubName);
                        pw.println("");
                    }
                    while(true) {
                        String message = reader.readLine();
                        messages = message.split(", ");
                        Event event = new Event(Long.parseLong(messages[0]), messages[1], messages[3]);
                        String channelname = messages[2];

                        sendToChannel(channelname, event);
                    }

                } else if (input.equals("subscriber")) {
                    System.out.println("The client chose to be a subscriber");

                    // send the list of channels to the subscriber
                    String channels = getChannels();
                    pw.println(channels);

                    String channelName = reader.readLine();     // waiting for the subscriber to send the name of the topic
                    if(channelName == null)
                        return;
                    Subscriber subscriber = new Subscriber(socket);
                    subscriber.subscribe(channelName);
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a comma sepearated string of the available channels
     * @return
     */
    public String getChannels(){
        StringBuilder channels = new StringBuilder(" ");
        for (Map.Entry<String, Channel> entry : Middleware.channelHashMap.entrySet()) {
            channels.append(entry.getKey() + ",");
        }
        channels.deleteCharAt(channels.toString().length() - 1);
        return channels.toString();
    }

    /**
     * Pushes a message to the queue of the specified channel
     * @param channelName Name of the channel
     * @param event The messages to be posted to the channel
     */
    public void sendToChannel(String channelName, Event event){
        // checking to see if the channel already exists.
        Channel channel = Middleware.channelHashMap.get(channelName);
        if(channel != null){
            // add message to the queue of channel
            channel.sendMessage(event);
        } else {
            channel = new Channel(channelName);
            Middleware.channelHashMap.put(channelName, channel);
            // creating the thread to start sending messages to consumers
            Thread channelThread = new Thread(channel);
            channelThread.start();
            channel.sendMessage(event);
        }


    }
}
