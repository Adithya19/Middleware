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
                        System.out.println("Client's message: "  + message);
                        messages = message.split(",");
                        String channelname = messages[0];
                        System.out.println("The channel is " + messages[0]); // splitting the channel name and message
                        System.out.println("The message is " + messages[1]);

                        sendToChannel(channelname, messages[1]);
                    }

                } else if (input.equals("subscriber")) {
                    System.out.println("The client chose to be a subscriber");

                    // send the list of channels to the subscriber
                    String channels = getChannels();
                    pw.println(channels);

                    String channelName = reader.readLine();     // waiting for the subscriber to send the name of the topic
                    Channel channel = Middleware.channelHashMap.get(channelName);
                    channel.subscribe(socket);
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
        StringBuilder channels = new StringBuilder("");
        for (Map.Entry<String, Channel> entry : Middleware.channelHashMap.entrySet()) {
            channels.append(entry.getKey() + ",");
        }
        channels.deleteCharAt(channels.toString().length() - 1);
        return channels.toString();
    }


//    /**
//     * Creaees a channel with the given name
//     * And also starts a thread for it
//     * @param name name of the Channel to be created
//     * @return the queue of the channel to add messages
//     */
//    public Queue<String> generateChannel(String name){
//
//        /**
//         *  if the channel does not exist then
//         *  assign a new Q, to the middleware map
//         *  create a new channel
//         */
//        Queue<String> queue = new LinkedList<>();
//        Middleware.channelQueues.put(name, queue);
//        Channel channel = new Channel(name, queue);
//        Middleware.channelHashMap.put(name, channel);
//
//
//        return queue;
//    }

    /**
     * Pushes a message to the queue of the specified channel
     * @param channelName Name of the channel
     * @param message The messages to be posted to the channel
     */
    public void sendToChannel(String channelName, String message){
        // checking to see if the channel already exists.
        Channel channel = Middleware.channelHashMap.get(channelName);
        if(channel != null){
            // add message to the queue of channel
            channel.sendMessage(message);
        } else {
            channel = new Channel(channelName);
            Middleware.channelHashMap.put(channelName, channel);
            // creating the thread to start sending messages to consumers
            Thread channelThread = new Thread(channel);
            channelThread.start();
            channel.sendMessage(message);
        }


    }
}
