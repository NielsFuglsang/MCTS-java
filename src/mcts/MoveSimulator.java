package mcts;

import problem.*;
import simulator.State;

public class MoveSimulator {


    public State performA1(ProblemSpec ps, Node node) {

        State currentState = node.state;
        State nextState;

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

        int currentFuel = currentState.getFuel();
        if (fuelConsumption > currentFuel) {
            return currentState;
        }

        // Sample move distance
        int moveDistance = sampleMoveDistance(ps, currentState);

        boolean verbose = false;
        // handle slip and breakdown cases, addition of steps handled in step method
        if (moveDistance == ProblemSpec.SLIP) {
            if (verbose) {
                System.out.println("\tSampled move distance=SLIP");
            }
            nextState = currentState.changeSlipCondition(true);
        } else if (moveDistance == ProblemSpec.BREAKDOWN) {
            if (verbose) {
                System.out.println("\tSampled move distance=BREAKDOWN");
            }
            nextState = currentState.changeBreakdownCondition(true);
        } else {
            if (verbose) {
                System.out.println("\tSampled move distance=" + moveDistance);
            }
            nextState = currentState.changePosition(moveDistance, ps.getN());
        }

        // handle fuel usage for level 2 and above
        if (ps.getLevel().getLevelNumber() > 1) {
            nextState = nextState.consumeFuel(fuelConsumption);
        }

        return nextState;
    }

    private int sampleMoveDistance(ProblemSpec ps, State currentState) {

        double[] moveProbs = getMoveProbs(ps, currentState);

        double p = Math.random();
        double pSum = 0;
        int move = 0;
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            pSum += moveProbs[k];
            if (p <= pSum) {
                move = ps.convertIndexIntoMove(k);
                break;
            }
        }
        return move;
    }

    private double[] getMoveProbs(ProblemSpec ps, State currentState) {

        // get parameters of current state
        Terrain terrain = ps.getEnvironmentMap()[currentState.getPos() - 1];
        int terrainIndex = ps.getTerrainIndex(terrain);
        String car = currentState.getCarType();
        String driver = currentState.getDriver();
        Tire tire = currentState.getTireModel();

        // calculate priors
        double priorK = 1.0 / ProblemSpec.CAR_MOVE_RANGE;
        double priorCar = 1.0 / ps.getCT();
        double priorDriver = 1.0 / ps.getDT();
        double priorTire = 1.0 / ProblemSpec.NUM_TYRE_MODELS;
        double priorTerrain = 1.0 / ps.getNT();
        double priorPressure = 1.0 / ProblemSpec.TIRE_PRESSURE_LEVELS;

        // get probabilities of k given parameter
        double[] pKGivenCar = ps.getCarMoveProbability().get(car);
        double[] pKGivenDriver = ps.getDriverMoveProbability().get(driver);
        double[] pKGivenTire = ps.getTireModelMoveProbability().get(tire);
        double pSlipGivenTerrain = ps.getSlipProbability()[terrainIndex];
        double[] pKGivenPressureTerrain = convertSlipProbs(ps, currentState, pSlipGivenTerrain);

        // use bayes rule to get probability of parameter given k
        double[] pCarGivenK = bayesRule(pKGivenCar, priorCar, priorK);
        double[] pDriverGivenK = bayesRule(pKGivenDriver, priorDriver, priorK);
        double[] pTireGivenK = bayesRule(pKGivenTire, priorTire, priorK);
        double[] pPressureTerrainGivenK = bayesRule(pKGivenPressureTerrain,
                (priorTerrain * priorPressure), priorK);

        // use conditional probability formula on assignment sheet to get what
        // we want (but what is it that we want....)
        double[] kProbs = new double[ProblemSpec.CAR_MOVE_RANGE];
        double kProbsSum = 0;
        double kProb;
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProb = pCarGivenK[k] * pDriverGivenK[k] *
                    pTireGivenK[k] * pPressureTerrainGivenK[k] * priorK;
            kProbsSum += kProb;
            kProbs[k] = kProb;
        }

        // Normalize
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProbs[k] /= kProbsSum;
        }

        return kProbs;
    }

    private double[] bayesRule(double[] condProb, double priorA, double priorB) {

        double[] swappedProb = new double[condProb.length];

        for (int i = 0; i < condProb.length; i++) {
            swappedProb[i] = (condProb[i] * priorA) / priorB;
        }
        return swappedProb;
    }

    private double[] convertSlipProbs(ProblemSpec ps, State currentState, double slipProb) {

        // Adjust slip probability based on tire pressure
        TirePressure pressure = currentState.getTirePressure();
        if (pressure == TirePressure.SEVENTY_FIVE_PERCENT) {
            slipProb *= 2;
        } else if (pressure == TirePressure.ONE_HUNDRED_PERCENT) {
            slipProb *= 3;
        }
        // Make sure new probability is not above max
        if (slipProb > ProblemSpec.MAX_SLIP_PROBABILITY) {
            slipProb = ProblemSpec.MAX_SLIP_PROBABILITY;
        }

        // for each terrain, all other action probabilities are uniform over
        // remaining probability
        double[] kProbs = new double[ProblemSpec.CAR_MOVE_RANGE];
        double leftOver = 1 - slipProb;
        double otherProb = leftOver / (ProblemSpec.CAR_MOVE_RANGE - 1);
        for (int i = 0; i < ProblemSpec.CAR_MOVE_RANGE; i++) {
            if (i == ps.getIndexOfMove(ProblemSpec.SLIP)) {
                kProbs[i] = slipProb;
            } else {
                kProbs[i] = otherProb;
            }
        }

        return kProbs;
    }

    private State performA2(State currentState, Action a) {

        if (currentState.getCarType().equals(a.getCarType())) {
            // changing to same car type does not change state but still costs a step
            // no cheap refill here, muhahaha
            return currentState;
        }

        return currentState.changeCarType(a.getCarType());
    }

    /**
     * Perform CHANGE_DRIVER action
     *
     * @param a a CHANGE_DRIVER action object
     * @return the next state
     */
    private State performA3(State currentState, Action a) { return currentState.changeDriver(a.getDriverType()); }

    /**
     * Perform the CHANGE_TIRES action
     *
     * @param a a CHANGE_TIRES action object
     * @return the next state
     */
    private State performA4(State currentState, Action a) {
        return currentState.changeTires(a.getTireModel());
    }

    /**
     * Perform the ADD_FUEL action
     *
     * @param a a ADD_FUEL action object
     * @return the next state
     */
    private State performA5(State currentState, Action a) {
        // calculate number of steps used for refueling (minus 1 since we add
        // 1 in main function
        int stepsRequired = (int) Math.ceil(a.getFuel() / (float) 10);
        return currentState.addFuel(a.getFuel());
    }

    /**
     * Perform the CHANGE_PRESSURE action
     *
     * @param a a CHANGE_PRESSURE action object
     * @return the next state
     */
    private State performA6(State currentState, Action a) {
        return currentState.changeTirePressure(a.getTirePressure());
    }

    /**
     * Perform the CHANGE_CAR_AND_DRIVER action
     *
     * @param a a CHANGE_CAR_AND_DRIVER action object
     * @return the next state
     */
    private State performA7(State currentState, Action a) {

        if (currentState.getCarType().equals(a.getCarType())) {
            // if car the same, only change driver so no sneaky fuel exploit
            return currentState.changeDriver(a.getDriverType());
        }
        return currentState.changeCarAndDriver(a.getCarType(),
                a.getDriverType());
    }

    /**
     * Perform the CHANGE_TIRE_FUEL_PRESSURE action
     *
     * @param a a CHANGE_TIRE_FUEL_PRESSURE action object
     * @return the next state
     */
    private State performA8(State currentState, Action a) {
        return currentState.changeTireFuelAndTirePressure(a.getTireModel(),
                a.getFuel(), a.getTirePressure());
    }

    public State PerformAction(State s, Action a) {
        State nextState;

        switch(a.getActionType().getActionNo()) {
            case 2:
                nextState = performA2(s, a);
                break;
            case 3:
                nextState = performA3(s, a);
                break;
            case 4:
                nextState = performA4(s, a);
                break;
            case 5:
                nextState = performA5(s, a);
                break;
            case 6:
                nextState = performA6(s, a);
                break;
            case 7:
                nextState = performA7(s, a);
                break;
            case 8:
                nextState = performA8(s, a);
            default: nextState = s;
        }

        return nextState;
    }

}
