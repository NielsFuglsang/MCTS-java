package problem;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

/**
 * An action in the MDP
 */
public class Action {

    /** The type of action **/
    private ActionType actionType;
    /** car type **/
    private String carType;
    /** driver type **/
    private String driverType;
    /** tire model **/
    private Tire tireModel;
    /** fuel to add **/
    private int fuel;
    /** tire pressure change **/
    private TirePressure tirePressure;

    /**
     * Constructor for CHANGE_CAR or CHANGE_DRIVER action.
     *
     * @param actionType
     * @param type a car type or driver type
     */
    public Action(ActionType actionType, String type) {
        if (actionType == ActionType.CHANGE_CAR) {
            this.actionType = actionType;
            carType = type;
        } else if (actionType == ActionType.CHANGE_DRIVER) {
            driverType = type;
            this.actionType = actionType;
        } else {
            throw new ValueException("Action type must be CHANGE_CAR or CHANGE_DRIVER");
        }
    }

    /**
     * Constructor for CHANGE_TYRES action.
     *
     * @param actionType
     * @param tireModel
     */
    public Action(ActionType actionType, Tire tireModel) {
        if (actionType == ActionType.CHANGE_TYRES) {
            this.actionType = actionType;
            this.tireModel = tireModel;
        } else {
            throw new ValueException("Action type must be CHANGE_TYRES");
        }
    }

    /**
     * Constructor for ADD_FUEL action.
     *
     * @param actionType
     * @param fuel amount of fuel to add
     */
    public Action(ActionType actionType, int fuel) {
        if (actionType == ActionType.ADD_FUEL) {
            this.actionType = actionType;
            if (fuel < ProblemSpec.FUEL_MIN || fuel > ProblemSpec.FUEL_MAX) {
                throw new ValueException("Fuel amount must be: 0 <= fuel <= 50");
            }
            this.fuel = fuel;
        } else {
            throw new ValueException("Action type must be ADD_FUEL");
        }
    }

    /**
     * Constructor for CHANGE_PRESSURE action.
     *
     * @param actionType
     * @param tirePressure
     */
    public Action(ActionType actionType, TirePressure tirePressure) {
        if (actionType == ActionType.CHANGE_PRESSURE) {
            this.actionType = actionType;
            this.tirePressure = tirePressure;
        } else {
            throw new ValueException("Action type must be CHANGE_PRESSURE");
        }
    }

    /**
     * Constructor for CHANGE_CAR_AND_DRIVER action.
     *
     * @param actionType
     * @param carType
     * @param driverType
     */
    public Action(ActionType actionType, String carType, String driverType) {
        if (actionType == ActionType.CHANGE_CAR_AND_DRIVER) {
            this.actionType = actionType;
            this.carType = carType;
            this.driverType = driverType;
        } else {
            throw new ValueException("Action type must be CHANGE_CAR_AND_DRIVER");
        }
    }

    /**
     * Constructor for CHANGE_TYRE_FUEL_PRESSURE action
     *
     * @param actionType
     * @param tireModel
     * @param fuel
     * @param tirePressure
     */
    public Action(ActionType actionType, Tire tireModel, int fuel, TirePressure tirePressure) {
        if (actionType == ActionType.CHANGE_TYRE_FUEL_PRESSURE) {
            this.actionType = actionType;
            this.tireModel = tireModel;
            if (fuel < ProblemSpec.FUEL_MIN || fuel > ProblemSpec.FUEL_MAX) {
                throw new ValueException("Fuel amount must be: 0 <= fuel <= 50");
            }
            this.fuel = fuel;
            this.tirePressure = tirePressure;
        } else {
            throw new ValueException("Action type must be CHANGE_TYRE_FUEL_PRESSURE");
        }
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getCarType() {
        return carType;
    }

    public String getDriverType() {
        return driverType;
    }

    public Tire getTireModel() {
        return tireModel;
    }

    public int getFuel() {
        return fuel;
    }

    public TirePressure getTirePressure() {
        return tirePressure;
    }
}
