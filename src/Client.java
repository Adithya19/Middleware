import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 3000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        System.out.println(reader.readLine());
        System.out.println("Choose your option and press Enter");
        System.out.println("1. Become a publisher");
        System.out.println("2. Become a subscriber");

        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        if(input.equals("1")){
        // create a publisher
            writer.println("publisher");
            System.out.println("Enter your name");
            String name = scanner.nextLine();
            writer.println(name);
            System.out.println(reader.readLine());
            Publisher publisher = new Publisher(writer, name);

            // looping to send the message
            while(true){
                System.out.println("What topic do you want to publish to?");
                String channel = scanner.nextLine();
                // you can add a check here to see if the topics chosen are valid.
                System.out.println("Enter the message to be sent");
                input = scanner.nextLine();
                input = input.trim();
                if(input.equals("DuplicateMessage")){
                    // invoke duplicate message
                    sendDuplicateMessage(publisher, name, channel);
                    continue;
                }
                // sending out the epoch time, pubid, channelname and the message
                publisher.publish(System.currentTimeMillis(), name, channel, input);   // send the message to the middleware
            }

        } else if(input.equals("2")){
        // create a subscriber
            writer.println("subscriber");

            // get a list of channels from the server
            String channels = reader.readLine();
            Arrays.stream(channels.split(",")).forEach(e -> System.out.println(e));

            System.out.println("Enter the channel you want to subscribe to");
            input = scanner.nextLine();
            writer.println(input);                      // send the channel you want to subscribe to
            while(true){
                input = reader.readLine();
                if(input == null)
                    break;
                System.out.println(input);  // waiting for messages from the server
            }
        } else {
            System.out.println("Invalid input");
        }
    }

    /**
     * This is a test involving sending duplicate messages to the subscriber
     * @param publisher
     * @param pubId
     * @param channelName
     */
    public static void sendDuplicateMessage(Publisher publisher, String pubId, String channelName){
        System.out.println("Sending out duplicate messages to the middleware");
        // send two messages with the same time stamp and pubid
        long epoch = System.currentTimeMillis();
        publisher.publish(epoch, pubId, channelName, "Duplicate message");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        publisher.publish(epoch, pubId, channelName, "Duplicate message");
    }
}
