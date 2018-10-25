package simulator;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import problem.*;

import java.io.IOException;

/**
 * This class is the simulator for the problem.
 * The simulator takes in an action and returns the next state.
 */
public class Simulator {

    /** Problem spec for the current problem **/
    private ProblemSpec ps;
    /** The current state of the environment **/
    private State currentState;
    /** The number of steps taken **/
    private int steps;

    /**
     * Construct a new simulator instance from the given problem spec
     *
     * @param ps the ProblemSpec
     */
    public Simulator(ProblemSpec ps) {
        this.ps = ps;
        currentState = State.getStartState(ps.getFirstCarType(),
                ps.getFirstDriver(), ps.getFirstTireModel());
        steps = 0;
    }

    /**
     * Construct a new simulator instance from the given input file
     *
     * @param fileName path to input file
     * @throws IOException if can't find file or there is a format error
     */
    public Simulator(String fileName) throws IOException {
        this(new ProblemSpec(fileName));
    }

    /**
     * Perform an action against environment and receive the next state
     *
     * @param a the action to perform
     * @return the next state
     */
    public State step(Action a) throws ValueException {

        State nextState;

        if (!actionValidForLevel(a)) {
            throw new IllegalArgumentException("ActionType A"
                    + a.getActionType().getActionNo()
                    + " is an invalid action for problem level "
                    + ps.getLevel());
        }

        switch(a.getActionType().getActionNo()) {
            case 1:
                nextState = performA1(a);
                break;
            case 2:
                nextState = performA2(a);
                break;
            case 3:
                nextState = performA3(a);
                break;
            case 4:
                nextState = performA4(a);
                break;
            case 5:
                nextState = performA5(a);
                break;
            case 6:
                nextState = performA6(a);
                break;
            case 7:
                nextState = performA7(a);
                break;
            default:
                nextState = performA8(a);
        }

        steps += 1;
        currentState = nextState.copyState();
        return nextState;
    }

    /**
     * Checks if given action is valid for the current problem level
     *
     * @param a the action being performed
     * @return True if action is value, False otherwise
     */
    private boolean actionValidForLevel(Action a) {
        return ps.getLevel().isValidActionForLevel(a.getActionType());
    }

    /**
     * Perform CONTINUE_MOVING action
     *
     * @param a a CONTINUE_MOVING action object
     * @return the next state
     */
    private State performA1(Action a) {

        State nextState;

        if (currentState.isInSlipCondition()) {
            return currentState.reduceSlipTimeLeft();
        }
        if (currentState.isInBreakdownCondition()) {
            return currentState.reduceBreakdownTimeLeft();
        }

        // check there is enough fuel to make move in current state
        int fuelRequired = getFuelConsumption();
        int currentFuel = currentState.getFuel();
        if (fuelRequired > currentFuel) {
            return currentState;
        }

        // Sample move distance
        int moveDistance = sampleMoveDistance();

        // handle slip and breakdown cases
        if (moveDistance == ProblemSpec.SLIP) {
            nextState =  currentState.changeSlipCondition(true,
                    ps.getSlipRecoveryTime());
        } else if (moveDistance == ProblemSpec.BREAKDOWN) {
            nextState = currentState.changeBreakdownCondition(true,
                    ps.getRepairTime());
        } else {
            nextState = currentState.changePosition(moveDistance, ps.getN());
        }

        // handle fuel usage for level 2 and above
        if (ps.getLevel().getLevelNumber() > 1) {
            nextState = nextState.consumeFuel(fuelRequired);
        }

        return nextState;
    }

    /**
     * Return the move distance by sampling from conditional probability
     * distribution.
     *
     * @return the move distance in range [-4, 5] or SLIP or BREAKDOWN
     */
    private int sampleMoveDistance() {

        // get parameters of current state
        Terrain terrain = ps.getEnvironmentMap()[currentState.getPos() - 1];
        String car = currentState.getCarType();
        String driver = currentState.getDriver();
        Tire tire = currentState.getTireModel();

        // calculate priors
        float priorK = 1 / ps.CAR_MOVE_RANGE;
        float priorCar = 1 / ps.getCT();
        float priorDriver = 1 / ps.getDT();
        float priorTire = 1 / ps.NUM_TYRE_MODELS;
        // TODO: prior for terrain?

        // get probabilities of k given parameter
        float[] pKGivenCar = ps.getCarMoveProbability().get(car);
        float[] pKGivenDriver = ps.getDriverMoveProbability().get(driver);
        float[] pKGivenTire = ps.getTireModelMoveProbability().get(tire);

        // use bayes rule to get probability of parameter given k
        float[] pCarGivenK = bayesRule(pKGivenCar, priorCar, priorK);
        float[] pDriverGivenK = bayesRule(pKGivenDriver, priorDriver, priorK);
        float[] pTireGivenK = bayesRule(pKGivenTire, priorTire, priorK);

        // use conditional probability formula on assignment sheet to get what
        // we want
        


        return -1;
    }

    /**
     * Apply bayes rule to all values in cond probs list.
     *
     * @param condProb list of P(B|A)
     * @param priorA prior probability of parameter A
     * @param priorB prior probability of parameter B
     * @return list of P(A|B)
     */
    private float[] bayesRule(float[] condProb, float priorA, float priorB) {

        float[] swappedProb = new float[condProb.length];

        for (int i = 0; i < condProb.length; i++) {
            swappedProb[i] = (condProb[i] * priorA) / priorB;
        }
        return swappedProb;
    }


    private int getFuelConsumption() {

        // get parameters of current state
        Terrain terrain = ps.getEnvironmentMap()[currentState.getPos() - 1];
        String car = currentState.getCarType();
        TirePressure pressure = currentState.getTirePressure();

        // get fuel consumption
        int terrainIndex = ps.getTerrainIndex(terrain);
        int carIndex = ps.getCarIndex(car);
        int fuelConsumption = ps.getFuelUsage()[terrainIndex][carIndex];

        if (pressure == TirePressure.FIFTY_PERCENT) {
            fuelConsumption *= 3;
        } else if (pressure == TirePressure.SEVENTY_FIVE_PERCENT) {
            fuelConsumption *= 2;
        }
        return fuelConsumption;
    }

    /**
     * Perform CHANGE_CAR action
     *
     * @param a a CHANGE_CAR action object
     * @return the next state
     */
    private State performA2(Action a) {
        return currentState.changeCarType(a.getCarType());
    }

    /**
     * Perform CHANGE_DRIVER action
     *
     * @param a a CHANGE_DRIVER action object
     * @return the next state
     */
    private State performA3(Action a) {
        return currentState.changeDriver(a.getDriverType());
    }

    /**
     * Perform the CHANGE_TYRES action
     *
     * @param a a CHANGE_TYRES action object
     * @return the next state
     */
    private State performA4(Action a) {
        return currentState.changeTires(a.getTireModel());
    }

    /**
     * Perform the ADD_FUEL action
     *
     * @param a a ADD_FUEL action object
     * @return the next state
     */
    private State performA5(Action a) {
        return currentState.addFuel(a.getFuel());
    }

    /**
     * Perform the CHANGE_PRESSURE action
     *
     * @param a a CHANGE_PRESSURE action object
     * @return the next state
     */
    private State performA6(Action a) {
        return currentState.changeTirePressure(a.getTirePressure());
    }

    /**
     * Perform the CHANGE_CAR_AND_DRIVER action
     *
     * @param a a CHANGE_CAR_AND_DRIVER action object
     * @return the next state
     */
    private State performA7(Action a) {
        return currentState.changeCarAndDriver(a.getCarType(),
                a.getDriverType());
    }

    /**
     * Perform the CHANGE_TYRE_FUEL_PRESSURE action
     *
     * @param a a CHANGE_TYRE_FUEL_PRESSURE action object
     * @return the next state
     */
    private State performA8(Action a) {
        return currentState.changeTireFuelAndTirePressure(a.getTireModel(),
                a.getFuel(), a.getTirePressure());
    }

    /**
     * Check whether a given state is the goal state or not
     *
     * @param s the state to check
     * @return True if s is goal state, False otherwise
     */
    public Boolean isGoalState(State s) {
        return s.getPos() >= ps.getN();
    }

}
