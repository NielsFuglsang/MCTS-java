package problem;

/**
 * Enum class for the possible tire pressures allowed in problem
 */
public enum TirePressure {
    FIFTY_PERCENT,
    SEVENTY_FIVE_PERCENT,
    ONE_HUNDRED_PERCENT;

    TirePressure tirePressure;
    private String text;

    static {
        FIFTY_PERCENT.text = "50%";
        SEVENTY_FIVE_PERCENT.text = "75%";
        ONE_HUNDRED_PERCENT.text = "100%";
    }

    public int getPressureIndex() {
        switch (tirePressure) {
            case FIFTY_PERCENT:
                return 1;
            case SEVENTY_FIVE_PERCENT:
                return 2;
            case ONE_HUNDRED_PERCENT:
                return 3;
        }
        return 0;
    }

    public String asString() {
        return text;
    }
}
