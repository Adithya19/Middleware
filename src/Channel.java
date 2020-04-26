import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Channel implements Runnable{
    String name;
    ArrayList<Socket> subscribers = new ArrayList<>();
    PrintWriter writer;
    Queue<String> messageQueue;
    ArrayList<Queue<String>> messageQueues = new ArrayList<>();
    ArrayList<Queue<String>> backupMessageQueues = new ArrayList<>();
    int queueSize = 2;
    public Channel(String name){
        this.name = name;
    }

//    public Channel(String name, Queue<String> queue){
//        this.messageQueue = queue;
//    }

    public void push(String message){
        System.out.println(subscribers);
        for ( Socket socket : subscribers) {
            try {
                writer = new PrintWriter(socket.getOutputStream());
                writer.println(message);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public boolean subscribe(Socket socket){
        subscribers.add(socket);
        return true;
    }

    @Override
    public void run() {
        String message;
        while(true){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!subscribers.isEmpty()){
                message = getMessage();
                if(message!=null) {
                    push(message);
                }
            }
        }
    }

    /**
     * Finds the earliest message to be sent from all the queues
     * @param message The message to be sent to everyone
     */
    public void sendMessage(String message){
        // find the last queue and add the message to that
        // if the last queue is full then create a new queue and add to that
        if(messageQueues.size() <= 0){
            Queue<String> newQueue = new LinkedList<>();
            Queue<String> newBackupQueue = new LinkedList<>();
            messageQueues.add(newQueue);
            backupMessageQueues.add(newBackupQueue);
            System.out.println("Size of new Q: " + messageQueues.size());
        }

        // getting hold of the working queue and the backup queue.
        Queue<String> queue = messageQueues.get(messageQueues.size() - 1);
        Queue<String> backupQueue = backupMessageQueues.get(backupMessageQueues.size() - 1);

        try{
            if(queue.size() >= queueSize){
                Queue<String> newQueue = new LinkedList<>();
                Queue<String> newBackupQueue = new LinkedList<>();
                // throw an excpetion for a certain message
                if(message.trim().equals("CrashQueue"))
                    // this is done to simulate the queue crashing when it tries to add messages
                    throw new Exception("Queue has been crashed");
                // adding the message to both the backup and the main queue
                newQueue.add(message);
                messageQueues.add(newQueue);

                newBackupQueue.add(message);
                backupMessageQueues.add(newBackupQueue);
            } else {
                queue.add(message);
                backupQueue.add(message);
            }
        }catch(Exception e){
            System.out.println("Queue crashed, switching it with the backup queue");
            // if the queue fails then switch it with the backup queue and create a new backup
            messageQueues = backupMessageQueues;
            // create a duplicate backup message queue
        }
        System.out.println("The message queuesss are : " + messageQueues);
    }

    public String getMessage(){
        // loop through all the queues
        Queue<String> queue = null;
        String message = null;
        for(int i = 0; i < messageQueues.size(); i++){
            queue = messageQueues.get(i);
            if(queue.size() <= 0){ // if any queue is empty then remove it
                messageQueues.remove(queue);
            } else {
                message = queue.remove();
            }
        }
        return message;
    }

}
