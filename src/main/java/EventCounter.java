public interface EventCounter {
    void addRecord();

    int getLastMinute();

    int getLastHour();

    void startReducingWorker();

    void startPrintingWorker(boolean withRange);

    int get(Long period);

    long getRange(Long period);
}
