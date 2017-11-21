public interface EventCounter {

    void submitEvent();

    long recordsForLastMinute();

    long recordsForLastHour();

    long recordsForLastDay();
}
