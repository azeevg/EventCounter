import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

public class EventCounterTest {

    @Test
    public void zeroTest() {
        EventCounter counter = new EventCounterImpl();
        long lastMinute = counter.getLastMinute();
        long lastHour = counter.getLastHour();

        Assert.assertEquals(0, lastMinute);
        Assert.assertEquals(0, lastHour);
    }

    @Test
    public void firstRecordTest() {
        EventCounter counter = new EventCounterImpl();
        counter.addRecord();
        long lastMinute = counter.getLastMinute();
        long lastHour = counter.getLastHour();

        Assert.assertEquals(1, lastMinute);
        Assert.assertEquals(1, lastHour);
    }

    @Test
    public void concurrent100InsertionsTest() throws InterruptedException {
        EventCounter counter = new EventCounterImpl();
        CountDownLatch latch = new CountDownLatch(2);

        new Thread(new RecordProducer("First", counter, latch, 50)).start();
        new Thread(new RecordProducer("Second", counter, latch, 50)).start();

        latch.await();

        Assert.assertEquals(100, counter.getLastHour());
        Assert.assertEquals(100, counter.getLastMinute());
    }


    @Test
    public void concurrentSimpleTest() throws InterruptedException {
        EventCounter counter = new EventCounterImpl();
        CountDownLatch latch = new CountDownLatch(2);

        new Thread(new RecordProducer("First", counter, latch, 6000)).start();
        new Thread(new RecordProducer("Second", counter, latch, 4000)).start();

        latch.await();

        Assert.assertEquals(10000, counter.getLastHour());
        Assert.assertEquals(10000, counter.getLastMinute());
    }

    @Test
    public void concurrentTest() throws InterruptedException {
        EventCounter counter = new EventCounterImpl();
        int n = 15;
        CountDownLatch latch = new CountDownLatch(n);



        for (int i = 0; i < n; i++) {
            new Thread(new RecordProducer(i + "", counter, latch, 1000)).start();
        }

        latch.await();

        Assert.assertEquals(15000, counter.getLastHour());
    }


    class RecordProducer implements Runnable {
        private final String name;
        private final EventCounter counter;
        private final CountDownLatch latch;
        private final int n;
        private final boolean logged;

        RecordProducer(String name, EventCounter counter, CountDownLatch latch, int n) {
         this(name, counter, latch, n, true);
        }

        public RecordProducer(String name, EventCounter counter, CountDownLatch latch, int n, boolean logged) {
            this.name = name;
            this.counter = counter;
            this.latch = latch;
            this.n = n;
            this.logged = logged;
        }

        @Override
        public void run() {
            if (logged) {
                System.out.println(name + " started.");
            }
            for (int i = 0; i < n; i++) {
                if (logged && i % (n / 10) == 0) {
                    System.out.println(name + " i=" + i);
                }
                counter.addRecord();
            }
            if (logged) {
                System.out.println(name + " finished.");
            }
            latch.countDown();
        }
    }
}