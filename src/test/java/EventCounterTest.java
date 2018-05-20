import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class EventCounterTest {

    @Test
    public void zeroTest() {
        EventCounter counter = new EventCounterImpl();
        long lastMinute = counter.getLastMinute();
        long lastHour = counter.getLastHour();

        Assert.assertEquals(lastMinute, 0);
        Assert.assertEquals(lastHour, 0);
    }

    @Test
    public void firstRecordTest() {
        EventCounter counter = new EventCounterImpl();
        counter.addRecord();
        long lastMinute = counter.getLastMinute();
        long lastHour = counter.getLastHour();

        Assert.assertEquals(lastMinute, 1);
        Assert.assertEquals(lastHour, 1);
    }

    class RecordProducer implements Runnable {
        private final String name;
        private final EventCounter  counter;
        private final CountDownLatch latch;
        private final int n;

        RecordProducer(String name, EventCounter counter, CountDownLatch latch, int n) {
            this.name = name;
            this.counter = counter;
            this.latch = latch;
            this.n = n;
        }

        @Override
        public void run() {
            System.out.println(name + " started.");
            for (int i = 0; i < n; i++) {
                if (i % 10 == 0) {
                    System.out.println(name + " i=" + i);
                }
                counter.addRecord();
            }
            System.out.println(name + " finished.");

            latch.countDown();
        }
    }

    @Test
    public void concurrent100InsertionsTest() throws InterruptedException {
        EventCounter counter = new EventCounterImpl();
        CountDownLatch latch = new CountDownLatch(2);

        new Thread(new RecordProducer("First", counter, latch, 50)).start();
        new Thread(new RecordProducer("Second", counter, latch, 50)).start();

        latch.await();

        Assert.assertEquals(counter.getLastHour(), 100);
        Assert.assertEquals(counter.getLastMinute(), 100);
    }
}
