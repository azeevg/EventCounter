import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventCounterImpl implements EventCounter {
    private static final Long HOUR_MILLIS = 3_600_000L;
    private static final Long MINUTE_MILLIS = 60_000L;
    private final ConcurrentLinkedQueue<Long> queueMinute = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Long> queueHour = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<Long, ConcurrentLinkedQueue<Long>> queues = new ConcurrentHashMap<>();

    public EventCounterImpl() {
        queues.put(MINUTE_MILLIS, queueMinute);
        queues.put(HOUR_MILLIS, queueHour);

        new Thread(() -> {
            try {
                while (true) {
                    for (Map.Entry<Long, ConcurrentLinkedQueue<Long>> entry : queues.entrySet()) {
                        cleanUp(entry.getValue(), entry.getKey());
                    }
                    wait(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void addRecord() {
        long currentTime = System.currentTimeMillis();
        queueHour.add(currentTime);
        queueMinute.add(currentTime);
    }

    @Override
    public long getLastMinute() {
        cleanUp(queueMinute, MINUTE_MILLIS);
        return queueMinute.size();
    }

    @Override
    public long getLastHour() {
        cleanUp(queueHour, HOUR_MILLIS);
        return queueHour.size();
    }

    private void cleanUp(ConcurrentLinkedQueue<Long> queue, long offset) {
        long currentTime = System.currentTimeMillis();
        while (queue.peek() != null && currentTime - queue.peek() > offset) {
            queue.remove();
        }
    }
}
