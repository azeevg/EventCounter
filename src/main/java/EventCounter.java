public interface EventCounter {
    void increment();
    long getLastMinute();
    long getLastHour();
}
