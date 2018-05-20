public enum PrintingMode {
    NONE(0),
    SIMPLE(1),
    RANGE(2);

    private int value;

    PrintingMode(int value) {
        this.value = value;
    }

    boolean isPrintingEnabled() {
        return this != NONE;
    }
}
