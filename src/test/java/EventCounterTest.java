import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

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

    private void runThreads(EventCounter counter, int threadsNumber, int recordsNumber, boolean print) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(threadsNumber);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < threadsNumber; i++) {
            if (i / 3 == 0) {
                long timeout = random.nextLong(100, 300);
                System.out.println("Sleep for " + timeout);
                TimeUnit.MILLISECONDS.sleep(timeout);
            }
            new Thread(new RecordProducer(i + "", counter, latch, recordsNumber, print)).start();
        }

        latch.await();

        Assert.assertEquals(threadsNumber * recordsNumber, counter.getLastHour());
    }

    @Test
    public void concurrentTest() throws InterruptedException {
        EventCounter counter = new EventCounterImpl();
        int threadsNumber = 15;
        int recordsNumber = 100000;

        runThreads(counter, threadsNumber, recordsNumber, true);
    }

    @Test
    public void customPeriodsTest() throws InterruptedException {
        EventCounter counter = new EventCounterImpl(PrintingMode.RANGE, 1000L, 2000L, EventCounterImpl.HOUR_MILLIS);
        int threadsNumber = 1;
        int recordsNumber = 100000;

        runThreads(counter, threadsNumber, recordsNumber, false);
        Assert.assertTrue(counter.getRange(1000L) <= 1000L);
        Assert.assertTrue(counter.getRange(2000L) <= 2000L);
        Assert.assertTrue(counter.getRange(EventCounterImpl.HOUR_MILLIS) <= EventCounterImpl.HOUR_MILLIS);
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

        RecordProducer(String name, EventCounter counter, CountDownLatch latch, int n, boolean logged) {
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