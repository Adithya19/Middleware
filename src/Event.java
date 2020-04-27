public class Event implements Comparable{
    String pubId;
    long timestamp;
    String payload;
    public Event(long timestamp, String pubId, String payload){
        this.timestamp = timestamp;
        this.pubId = pubId;
        this.payload = payload;
    }

    @Override
    public boolean equals(Object object){
        Event event = (Event) object;
        if(this.timestamp == event.timestamp && this.pubId.equals(event.pubId))
            return true;

        return false;
    }


    @Override
    public int compareTo(Object o) {
        if(timestamp > ((Event)o).timestamp)
            return 1;
        else if (timestamp < ((Event)o).timestamp)
            return -1;
        else
            return 0;
    }
}
