public interface EventCounter {
    void addRecord();
    long getLastMinute();
    long getLastHour();
}
