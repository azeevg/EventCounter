public interface EventRecorder {
    long MINUTE = 60 * 1000;
    long HOUR = MINUTE * 60;
    long DAY = HOUR * 24;

    void record(Event event);

    long recordsForLastPeriod(long timePeriod);

    default long recordsForLastMinute() {
        return recordsForLastPeriod(MINUTE);
    }

    default long recordsForLastHour() {
        return recordsForLastPeriod(HOUR);
    }

    default long recordsForLastDay() {
        return recordsForLastPeriod(DAY);
    }
}
