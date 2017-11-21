import java.util.Queue;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class SerialEventCounter implements EventCounter {
//    private final static long MINUTE = 60 * 1000;
//    private final static long HOUR = MINUTE * 60;
//    private final static long DAY = HOUR * 24;

    private final static long MINUTE = 1000;
    private final static long HOUR = 1500;
    private final static long DAY = 3000;

    private final EventsBucket rootBucket;
    private volatile int freqMillis = 1;


    public SerialEventCounter(int freqMillis) {
        this.freqMillis = freqMillis;
        rootBucket = new EventsBucket(MINUTE).joinBucket(HOUR).joinBucket(DAY);

        final Thread bucketsVisitor = new Thread(() -> {
            try {
                EventsBucket bucket = rootBucket;

                for (; ; ) {
                    TimeUnit.MILLISECONDS.sleep(freqMillis);
                    final long currTime = System.currentTimeMillis();

                    for (; ; ) {
                        Queue<Long> events = bucket.events;
                        while (!events.isEmpty() && currTime - events.peek() >= bucket.period) {
                            Long t = events.poll();
                            if (bucket.last != bucket) {
                                bucket.next.events.add(t);
                            }
                        }

                        if (bucket.last == bucket) {
                            break;
                        }

                        bucket = bucket.next;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        bucketsVisitor.setDaemon(true);
        bucketsVisitor.start();
    }

    @Override
    public void submitEvent() {
        rootBucket.events.add(System.currentTimeMillis());
    }

    @Override
    public long recordsForLastMinute() {
        return rootBucket.events.size();
    }

    @Override
    public long recordsForLastHour() {
        return recordsForLastMinute() + rootBucket.next.events.size();
    }

    @Override
    public long recordsForLastDay() {
        return recordsForLastHour() + rootBucket.next.next.events.size();
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(" | ");
        EventsBucket bucket = rootBucket;
        for (; ; ) {
            Queue<Long> events = bucket.events;
            joiner.add(events.toString());

            if (bucket.last == bucket) {
                break;
            }

            bucket = bucket.next;
        }

        return "PRINT EVENT COUNTER\n" + joiner.toString();
    }
}
