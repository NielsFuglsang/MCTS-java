package simulator;

import problem.ProblemSpec;
import problem.Tire;
import problem.TirePressure;

/**
 * An immutable class representing a state in the game changing technology
 * environment, defined by:
 *
 * - position of the car (i.e. the cell index)
 * - whether the car is in slip condition
 * - slip time remaining
 * - whether the car is in breakdown condition
 * - breakdown time remaining
 * - car type
 * - car fuel
 * - car tire pressure
 * - the driver
 * - tire model
 */
public class State {

    /** The position of the car in terms of grid cell in environment **/
    private int pos;
    /** Whether the car is in a slip condition **/
    private boolean slip;
    /** Time remaining on slip condition **/
    private int slipTimeLeft;
    /** Whether the car is broken down **/
    private boolean breakdown;
    /** Time remaining on breakdown condition **/
    private int breakdownTimeLeft;
    /** The car type **/
    private String carType;
    /** Fuel remaining **/
    private int fuel;
    /** Tire pressure **/
    private TirePressure tirePressure;
    /** The driver **/
    private String driver;
    /** The tire model **/
    private Tire tireModel;

    /**
     * Construct a new state with the given parameter values
     *
     * @param pos cell index of car in environment
     * @param slip if car is in slip condition
     * @param slipTimeLeft time remaining on slip if in slip
     * @param breakdown if car is in breakdown condition
     * @param breakdownTimeLeft time remaining on breakdown if in breakdown
     * @param carType the type of car
     * @param fuel the amount of fuel
     *        (assumes ProblemSpec.FUEL_MIN <= fuel <= ProblemSpec.FUEL_MAX)
     * @param tirePressure the pressure of tires
     * @param driver the driver
     * @param tireModel the model of the tires
     */
    public State(int pos, boolean slip, int slipTimeLeft, boolean breakdown,
                 int breakdownTimeLeft, String carType, int fuel,
                 TirePressure tirePressure, String driver, Tire tireModel) {
        this.pos = pos;
        this.slip = slip;
        this.slipTimeLeft = slipTimeLeft;
        this.breakdown = breakdown;
        this.breakdownTimeLeft = breakdownTimeLeft;
        this.carType = carType;
        this.fuel = fuel;
        this.tirePressure = tirePressure;
        this.driver = driver;
        this.tireModel = tireModel;
    }


    /**
     * Get the initial state of the problem
     *
     * @param carType the initial car type
     * @param driver the initial driver
     * @param tire the initial tire model
     * @return the initial state
     */
    public static State getStartState(String carType, String driver,
                                      Tire tire) {
        return new State(0, false, 0, false, 0, carType, ProblemSpec.FUEL_MAX,
                TirePressure.ONE_HUNDRED_PERCENT, driver, tire);
    }

    /**
     * Return the next state after moving the cars position from current state.
     *
     * N.B. move distance should be in range:
     *      [ProblemSpec.CAR_MIN_MOVE, ProblemSpec.CAR_MAX_MOVE]
     *
     * @param move the distance to move
     * @param N the max position (i.e. the goal region)
     * @return the next state
     */
    public State changePosition(int move, int N) {
        State nexState = copyState();
        if (nexState.pos + move > N) {
            nexState.pos = N;
        } else if (nexState.pos + move < 1) {
            // not zero indexed as per assignment spec
            nexState.pos = 1;
        } else {
            nexState.pos += move;
        }
        return nexState;
    }

    /**
     * Return the next state after changing the slip condition and slip time
     * left of current state
     *
     * @param newSlip the new slip condition
     * @param newSlipTimeLeft the new slip time left
     * @return the next state
     */
    public State changeSlipCondition(boolean newSlip, int newSlipTimeLeft) {
        State nextState = copyState();
        nextState.slip = newSlip;
        nextState.slipTimeLeft = newSlipTimeLeft;
        return nextState;
    }

    /**
     * Return the next state after reducing the slip time left counter by one
     * step
     *
     * @return the next state
     */
    public State reduceSlipTimeLeft() {
        int newSlipTime = slipTimeLeft - 1;
        if (newSlipTime <= 0) {
            return changeSlipCondition(false, 0);
        } else {
            return changeSlipCondition(slip, newSlipTime);
        }
    }

    /**
     * Return the next state after changing the breakdown condition and
     * breakdown time left of current state
     *
     * @param newBreakdown the new breakdown condition
     * @param newBreakdownTimeLeft new breakdown time left
     * @return the next state
     */
    public State changeBreakdownCondition(boolean newBreakdown,
                                          int newBreakdownTimeLeft) {
        State nextState = copyState();
        nextState.breakdown = newBreakdown;
        nextState.breakdownTimeLeft = newBreakdownTimeLeft;
        return nextState;
    }

    /**
     * Return the next state after reducing the breakdown repair timer by one
     *
     * @return the next state
     */
    public State reduceBreakdownTimeLeft() {
        int newBreakdownTime = breakdownTimeLeft - 1;
        if (newBreakdownTime <= 0) {
            return changeBreakdownCondition(false, 0);
        } else {
            return changeBreakdownCondition(breakdown, newBreakdownTime);
        }
    }

    /**
     * Return the next state after changing car type in current state.
     *
     * @param newCarType the new car type
     * @return the next state
     */
    public State changeCarType(String newCarType) {
        State nextState = copyState();
        nextState.carType = newCarType;
        nextState.fuel = ProblemSpec.FUEL_MAX;
        nextState.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
        return nextState;
    }

    /**
     * Return the next state after changing the driver in current state.
     *
     * @param newDriver the new driver
     * @return the next state
     */
    public State changeDriver(String newDriver) {
        State nextState = copyState();
        nextState.driver = newDriver;
        return nextState;
    }

    /**
     * Return the next state after changing the tire model in current state.
     *
     * @param newTire the new tire model
     * @return the next state
     */
    public State changeTires(Tire newTire) {
        State nextState = copyState();
        nextState.tireModel = newTire;
        nextState.tirePressure = TirePressure.ONE_HUNDRED_PERCENT;
        return nextState;
    }

    /**
     * Return the next state after adding fuel to current state. Amount of fuel
     * in a state is capped at ProblemSpec.FUEL_MAX
     *
     * @param fuelToAdd amount of fuel to add
     * @return the next state
     */
    public State addFuel(int fuelToAdd) {
        if (fuelToAdd > 0) {
            throw new IllegalArgumentException("Fuel to add must be positive");
        }

        State nextState = copyState();
        if (nextState.fuel + fuelToAdd > ProblemSpec.FUEL_MAX) {
            nextState.fuel = ProblemSpec.FUEL_MAX;
        } else {
            nextState.fuel += fuelToAdd;
        }
        return nextState;
    }

    /**
     * Return the next state after consuming fuel in current state.
     *
     * @param fuelConsumed amount of fuel consumed
     * @return the next state
     */
    public State consumeFuel(int fuelConsumed) {
        if (fuelConsumed > 0) {
            throw new IllegalArgumentException("Fuel consumed must be positive");
        }
        State nextState = copyState();
        nextState.fuel -= fuelConsumed;
        if (nextState.fuel < ProblemSpec.FUEL_MIN) {
            throw new IllegalArgumentException("Too much fuel consumed: "
                    + fuelConsumed);
        }
        return nextState;
    }

    /**
     * Return the next state after changing the tire pressure in current state.
     *
     * @param newTirePressure the new tire pressure
     * @return the next state
     */
    public State changeTirePressure(TirePressure newTirePressure) {
        State nextState = copyState();
        nextState.tirePressure = newTirePressure;
        return nextState;
    }

    /**
     * Return the next state after changing the car type and the driver in
     * current state.
     *
     * @param newCarType the new car type
     * @param newDriver the new driver
     * @return the next state
     */
    public State changeCarAndDriver(String newCarType, String newDriver) {
        State nextState = copyState();
        nextState.carType = newCarType;
        nextState.driver = newDriver;
        return nextState;
    }

    /**
     * Return the next state after changing the tire model, adding fuel and
     * changing the tire pressure
     *
     * @param newTireModel new tire model
     * @param fuelToAdd the amount of fuel to add
     * @param newTirePressure the new tire pressure
     * @return the next state
     */
    public State changeTireFuelAndTirePressure(Tire newTireModel, int fuelToAdd,
                                               TirePressure newTirePressure) {
        State nextState = addFuel(fuelToAdd);
        nextState.tireModel = newTireModel;
        nextState.tirePressure = newTirePressure;
        return nextState;
    }

    /**
     * Copy this state, returning a deep copy
     *
     * @return deep copy of current state
     */
    public State copyState() {
        return new State(pos, slip, slipTimeLeft, breakdown, breakdownTimeLeft,
                carType, fuel, tirePressure, driver, tireModel);
    }

    public int getPos() {
        return pos;
    }

    public boolean isInSlipCondition() {
        return slip;
    }

    public int getSlipTimeLeft() {
        return slipTimeLeft;
    }

    public boolean isInBreakdownCondition() {
        return breakdown;
    }

    public int getBreakdownTimeLeft() {
        return breakdownTimeLeft;
    }

    public String getCarType() {
        return carType;
    }

    public int getFuel() {
        return fuel;
    }

    public TirePressure getTirePressure() {
        return tirePressure;
    }

    public String getDriver() {
        return driver;
    }

    public Tire getTireModel() {
        return tireModel;
    }
}
