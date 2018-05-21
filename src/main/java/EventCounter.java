public interface EventCounter {
    void addRecord();
    int getLastMinute();
    int getLastHour();
    int get(Long period);
    long getRange(Long period);
}
