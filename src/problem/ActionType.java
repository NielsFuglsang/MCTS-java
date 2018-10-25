package problem;

/**
 * List of possible actions
 */
public enum ActionType {
    CONTINUE_MOVING,            // A1
    CHANGE_CAR,                 // A2
    CHANGE_DRIVER,              // A3
    CHANGE_TYRES,               // A4
    ADD_FUEL,                   // A5
    CHANGE_PRESSURE,            // A6
    CHANGE_CAR_AND_DRIVER,      // A7
    CHANGE_TYRE_FUEL_PRESSURE;  // A8

    private int actionNo;

    static {
        CONTINUE_MOVING.actionNo = 1;
        CHANGE_CAR.actionNo = 2;
        CHANGE_DRIVER.actionNo = 3;
        CHANGE_TYRES.actionNo = 4;
        ADD_FUEL.actionNo = 5;
        CHANGE_PRESSURE.actionNo = 6;
        CHANGE_CAR_AND_DRIVER.actionNo = 7;
        CHANGE_TYRE_FUEL_PRESSURE.actionNo = 8;
    }

    public int getActionNo() {
        return actionNo;
    }
}
