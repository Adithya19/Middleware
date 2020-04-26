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
//            System.out.println("Enter a unique name");
//            String name = scanner.nextLine();
            writer.println("publisher");
            System.out.println("Enter your name");
            String name = scanner.nextLine();
            writer.println(name);
            System.out.println(reader.readLine());
            Publisher publisher = new Publisher(writer, name);
//            System.out.println("What is the message that you want to publish?");

            // looping to send the message
            while(true){
                System.out.println("What topic do you want to publish to?");
                String channel = scanner.nextLine();
                // you can add a check here to see if the topics chosen are valid.
                System.out.println("Enter the message to be sent");
                input = scanner.nextLine();
                publisher.publish(channel, input);   // send the message to the middleware
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
}
