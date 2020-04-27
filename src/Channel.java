import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class Channel implements Runnable{
    String name;
    ArrayList<Socket> subscribers = new ArrayList<>();
    PrintWriter writer;
    Queue<String> messageQueue;
    ArrayList<Queue<Event>> messageQueues = new ArrayList<>();
    ArrayList<Queue<Event>> backupMessageQueues = new ArrayList<>();
    int queueSize = 2;
    public Channel(String name){
        this.name = name;
    }


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
        System.out.println("Starting channel thread");
        Event event;
        while(true){
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!subscribers.isEmpty()){
                event = getEvent();
                if(event!=null) { // there are messages to send
                    // need to check if this is a duplicate message
                    // if so then don't send anything because another one will be sent later
                    if(checkForDuplicates(event))
                        continue;
                    push(event.payload);
                }
            }
        }
    }

    /**
     * Finds the earliest message to be sent from all the queues
     * @param event The message to be sent to everyone
     */
    public void sendMessage(Event event){
        // find the last queue and add the message to that
        // if the last queue is full then create a new queue and add to that
        if(messageQueues.size() <= 0){
            Queue<Event> newQueue = new LinkedList<>();
            Queue<Event> newBackupQueue = new LinkedList<>();
            messageQueues.add(newQueue);
            backupMessageQueues.add(newBackupQueue);
//            System.out.println("Size of new Q: " + messageQueues.size());
        }

        // getting hold of the working queue and the backup queue.
        Queue<Event> queue = messageQueues.get(messageQueues.size() - 1);
        Queue<Event> backupQueue = backupMessageQueues.get(backupMessageQueues.size() - 1);

        try{
            if(queue.size() >= queueSize){
                Queue<Event> newQueue = new LinkedList<>();
                Queue<Event> newBackupQueue = new LinkedList<>();
                // throw an excpetion for a certain message
                if(event.payload.trim().equals("CrashQueue"))
                    // this is done to simulate the queue crashing when it tries to add messages
                    throw new Exception("Queue has been crashed");
                // adding the message to both the backup and the main queue
                newQueue.add(event);
                messageQueues.add(newQueue);

                newBackupQueue.add(event);
                backupMessageQueues.add(newBackupQueue);
            } else {
                if(event.payload.trim().equals("CrashQueue"))
                    // this is done to simulate the queue crashing when it tries to add messages
                    throw new Exception("Queue has been crashed");
                queue.add(event);
                backupQueue.add(event);
            }
        }catch(Exception e){
            System.out.println("Queue crashed, switching it with the backup queue");
            // if the queue fails then switch it with the backup queue and create a new backup
            messageQueues = backupMessageQueues;
            // create a duplicate backup message queue
        }
//        System.out.println("The message queuesss are : " + messageQueues);
    }

    public Event getEvent(){
        // loop through all the queues
        Queue<Event> queue = null;
        Event event = null;
        for(int i = 0; i < messageQueues.size(); i++){
            queue = messageQueues.get(i);
//            System.out.println("Looking at Q: " + i + ". " + queue);
            if(queue.size() <= 0){ // if any queue is empty then remove it
//                System.out.println("Removing Q: " + i);
                messageQueues.remove(queue);
            } else {
                event = queue.remove();
                if(event == null){ // the queue contains null messages
                    messageQueues = backupMessageQueues;
                    i--;
                    continue;
                }
                break;
            }
        }
        return event;
    }

    /**
     * Checks all the queues to see if there is duplciate message
     * Messaegs with the same timestamp and same sender
     * @param event
     * @return
     */
    public synchronized boolean checkForDuplicates(Event event){
        // loop through all the queues and check tf there is a duplicate message

        Iterator<Queue<Event>> iter = messageQueues.iterator();

        for(int i = 0; i < messageQueues.size(); i++){
            Queue<Event> queue = messageQueues.get(i);
            if(queue.contains(event)){
                return true;
            }
        }

        return false;
    }
}
