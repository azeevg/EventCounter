import org.junit.Assert;
import org.junit.Test;

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

}
