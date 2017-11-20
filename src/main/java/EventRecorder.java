public interface EventRecorder {
    long MINUTE = 60 * 1000;
    long HOUR = MINUTE * 60;
    long DAY = HOUR * 24;

    void record(Event event);
    long recordsForLastMinute();
    long recordsForLastHour();
    long recordsForLastDay();
}
