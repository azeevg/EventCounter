import java.util.Iterator;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EventCounterImpl implements EventCounter {
    static final Long HOUR_MILLIS = 3_600_000L;
    private static final Long MINUTE_MILLIS = 60_000L;
    private static final long PRINTING_DELAY = 100;
    private static final long PRINTING_INITIAL_DELAY = 0;
    private static final long REDUCING_DELAY = 100;
    private static final long REDUCING_INITIAL_DELAY = 1000;
    private final ConcurrentHashMap<Long, ConcurrentLinkedQueue<Long>> queues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Lock> locks = new ConcurrentHashMap<>();
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    EventCounterImpl() {
        this(MINUTE_MILLIS, HOUR_MILLIS);
    }

    EventCounterImpl(long... periods) {
        for (long period : periods) {
            queues.put(period, new ConcurrentLinkedQueue<>());
            locks.put(period, new ReentrantLock());
        }
    }

    @Override
    public void startPrintingWorker(boolean includingRange) {
        Runnable printingWorker = getPrintingWorker(includingRange);
        executor.scheduleWithFixedDelay(printingWorker, PRINTING_INITIAL_DELAY, PRINTING_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void startReducingWorker() {
        Runnable reducingWorker = getReducingWorker();
        executor.scheduleWithFixedDelay(reducingWorker, REDUCING_INITIAL_DELAY, REDUCING_DELAY, TimeUnit.MILLISECONDS);
    }

    private Runnable getReducingWorker() {
        return () -> queues.keySet().forEach(period -> {
            ConcurrentLinkedQueue<Long> queue = getQueue(period);
            cleanupQueue(period, queue);
        });
    }

    private Runnable getPrintingWorker(boolean includingRange) {
        return () -> {
            final StringJoiner joiner = new StringJoiner(", ");
            queues.forEach((key, value) -> {
                if (includingRange) {
                    joiner.add(getRange(key) + " / " + key + " (" + get(key) + ")");
                } else {
                    joiner.add(key / 1000 + "(" + get(key) + ")");
                }
            });
            System.out.println(joiner.toString());
        };
    }

    @Override
    public void addRecord() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Long, ConcurrentLinkedQueue<Long>> entry : queues.entrySet()) {
            synchronized (locks.get(entry.getKey())) {
                entry.getValue().add(currentTime);
            }
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
        ConcurrentLinkedQueue<Long> queue = getQueue(period);
        synchronized (locks.get(period)) {
            cleanupQueue(period, queue);
            return queue.size();
        }
    }

    @Override
    public long getRange(Long period) {
        ConcurrentLinkedQueue<Long> queue = getQueue(period);
        synchronized (locks.get(period)) {
            cleanupQueue(period, queue);
            Iterator<Long> iterator = queue.iterator();

            if (!iterator.hasNext()) {
                return 0L;
            } else {
                Long head = iterator.next();
                Long tail = head;
                while (iterator.hasNext()) {
                    tail = iterator.next();
                }

                return tail - head;
            }
        }
    }

    private ConcurrentLinkedQueue<Long> getQueue(Long period) {
        ConcurrentLinkedQueue<Long> queue = queues.get(period);

        if (queue == null) {
            throw new IllegalArgumentException("There is no queue for " + period);
        }

        return queue;
    }

    private void cleanupQueue(Long period, ConcurrentLinkedQueue<Long> queue) {
        System.out.println("Cleanup started: " + period / 1000);
        int removed = 0;
        final long currentTime = System.currentTimeMillis();
        synchronized (locks.get(period)) {
            while (queue.peek() != null && currentTime - queue.peek() > period) {
                queue.remove();
                removed++;
            }
        }
        System.out.println("Cleanup finished. Removed: " + removed);
    }
}
