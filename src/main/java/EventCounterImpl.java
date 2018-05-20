import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EventCounterImpl implements EventCounter {
    static final Long HOUR_MILLIS = 3_600_000L;
    private static final Long MINUTE_MILLIS = 60_000L;
    private final ConcurrentHashMap<Long, ConcurrentLinkedQueue<Long>> queues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Lock> locks = new ConcurrentHashMap<>();

    EventCounterImpl() {
        this(false, MINUTE_MILLIS, HOUR_MILLIS);
    }

    EventCounterImpl(boolean print, long... periods) {
        for (long period : periods) {
            queues.put(period, new ConcurrentLinkedQueue<>());
            locks.put(period, new ReentrantLock());
        }



        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        Runnable reducingWorker = () -> queues.keySet().forEach(this::cleanupQueue);
        executor.scheduleWithFixedDelay(reducingWorker, 10, 300, TimeUnit.MILLISECONDS);

        if (print) {
            Runnable printingWorker = () -> {
                final StringJoiner joiner = new StringJoiner(", ");
                queues.forEach((key, value) -> joiner.add(key / 1000 + ":" + get(key)));
                System.out.println(joiner.toString());
            };
            executor.scheduleWithFixedDelay(printingWorker, 0, 10, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void addRecord() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Long, ConcurrentLinkedQueue<Long>> entry : queues.entrySet()) {
            entry.getValue().add(currentTime);
        }
    }

    @Override
    public int getLastMinute() {
        return get(MINUTE_MILLIS);
    }

    @Override
    public int getLastHour() {
        return get(HOUR_MILLIS);
    }

    @Override
    public int get(Long period) {
        ConcurrentLinkedQueue<Long> queue = queues.get(period);

        if (queue == null) {
            throw new IllegalArgumentException("There is no queue for " + period);
        }
        cleanupQueue(period);
        return queue.size();
    }

    private void cleanupQueue(Long period) {
        long currentTime = System.currentTimeMillis();
        ConcurrentLinkedQueue<Long> queue = queues.get(period);
        while (queue.peek() != null && currentTime - queue.peek() > period) {
            queue.remove();
        }
    }
}
