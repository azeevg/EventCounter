import java.util.concurrent.ConcurrentLinkedQueue;

public class EventCounterImpl implements EventCounter {

    private final ConcurrentLinkedQueue<Long> queueMinute = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Long> queueHour = new ConcurrentLinkedQueue<>();

    @Override
    public void addRecord() {
        long currentTime = System.currentTimeMillis();

        synchronized (queueHour) {
            queueHour.add(currentTime);
        }
        synchronized (queueMinute) {
            queueMinute.add(currentTime);
        }
    }

    @Override
    public long getLastMinute() {
        return queueMinute.size();
    }

    @Override
    public long getLastHour() {
        return queueHour.size();
    }
}
