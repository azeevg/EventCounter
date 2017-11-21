import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EventsBucket {
    final Queue<Long> events = new ConcurrentLinkedQueue<>();
    final long period;
    EventsBucket last = this;
    EventsBucket next = this;

    public EventsBucket(long period) {
        this.period = period;
    }

    public EventsBucket joinBucket(long period) {
        if (period <= last.period) {
            throw new IllegalArgumentException("Periods should increase from bucket to bucket.");
        }

        if (last == this) {
            next = last = new EventsBucket(period);
            last.last = last;
        } else {
            last.last = last.next = new EventsBucket(period);
            last = last.next;
        }

        return this;
    }
}
