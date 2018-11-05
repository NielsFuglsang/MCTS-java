package simulator;

import problem.*;
import simulator.State;

import java.util.HashMap;
import java.util.List;


public class ValueIteration {
    ProblemSpec ps;
    HashMap<State, Double> valueIteration = new HashMap<>();
    HashMap<State, Integer> policy = new HashMap<>();
    HashMap<State, Double> valueIterationTmp = new HashMap<>();
    HashMap<State, Integer> policyTmp = new HashMap<>();
    HashMap<State, Double> reward = new HashMap<>();
    List<String> cars;
    List<String> drivers;
    List<Tire> tires;

    public ValueIteration(ProblemSpec ps) {
        this.ps = ps;
        cars = ps.getCarOrder();
        drivers = ps.getDriverOrder();
        tires = ps.getTireOrder();
    }

    public void valueIteration() {
        State state;
        State newState;
        //Go through each state
        for(int pos=0; pos<ps.getN(); pos++) {
            for(int car=0; car<ps.getCT(); car++) {
                for(int driver=0; driver<ps.getDT(); driver++) {
                    for(int tire=0; tire<ps.NUM_TYRE_MODELS; tire++) {
                        if(ps.getLevel().getLevelNumber() == 1) {
                            state = new State(pos, false, false, cars.get(car),
                                    50, TirePressure.ONE_HUNDRED_PERCENT, drivers.get(driver), tires.get(tire));
                            double bestValue = 0.0;
                            int bestAction = 1;
                            for(ActionType a : ps.getLevel().getAvailableActions()) {
                                if(a.getActionNo() == 1) { //Move
                                    double newValue = reward.get(state) + sumTransitionFunc(state) * ps.getDiscountFactor();
                                    if (bestValue < newValue) {
                                        bestValue = newValue;
                                        bestAction = a.getActionNo();
                                    }
                                } else if(a.getActionNo() == 2) { //Change car
                                    newState = new State(pos, false, false, cars.get(car + 1 % 2),
                                            50, TirePressure.ONE_HUNDRED_PERCENT, drivers.get(driver), tires.get(tire));
                                    double newValue = reward.get(state) + valueIteration.get(newState) * ps.getDiscountFactor();
                                    if (bestValue < newValue) {
                                        bestValue = newValue;
                                        bestAction = a.getActionNo();
                                    }
                                } else if(a.getActionNo() == 3) { //Change driver
                                    newState = new State(pos, false, false, cars.get(car),
                                            50, TirePressure.ONE_HUNDRED_PERCENT, drivers.get(driver + 1 % 2), tires.get(tire));
                                    double newValue = reward.get(state) + valueIteration.get(newState) * ps.getDiscountFactor();
                                    if (bestValue < newValue) {
                                        bestValue = newValue;
                                        bestAction = a.getActionNo();
                                    }
                                } else if(a.getActionNo() == 4) { //Change tires
                                    for (int newTire=0; newTire<ps.NUM_TYRE_MODELS - 1; newTire++) {
                                        newState = new State(pos, false, false, cars.get(car),
                                                50, TirePressure.ONE_HUNDRED_PERCENT, drivers.get(driver), tires.get(newTire % ps.NUM_TYRE_MODELS));
                                        double newValue = reward.get(state) + valueIteration.get(newState) * ps.getDiscountFactor();
                                        if (bestValue < newValue) {
                                            bestValue = newValue;
                                            bestAction = a.getActionNo();
                                        }
                                    }
                                }
                                valueIterationTmp.put(state, bestValue);
                                policyTmp.put(state, bestAction);
                            }
                        }
                    }
                }
            }
        }
        valueIteration = valueIterationTmp;
        policy = policyTmp;
    }

    public double sumTransitionFunc(State s) {
        Terrain terrain = ps.getEnvironmentMap()[s.getPos() - 1];
        int terrainIndex = ps.getTerrainIndex(terrain);
        String car = s.getCarType();
        String driver = s.getDriver();
        Tire tire = s.getTireModel();

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
        //double[] pKGivenPressureTerrain = convertSlipProbs(pSlipGivenTerrain);

        // use bayes rule to get probability of parameter given k
        double[] pCarGivenK = bayesRule(pKGivenCar, priorCar, priorK);
        double[] pDriverGivenK = bayesRule(pKGivenDriver, priorDriver, priorK);
        double[] pTireGivenK = bayesRule(pKGivenTire, priorTire, priorK);
        //double[] pPressureTerrainGivenK = bayesRule(pKGivenPressureTerrain, (priorTerrain * priorPressure), priorK);


        // use conditional probability formula on assignment sheet to get what
        // we want (but what is it that we want....)
        double[] kProbs = new double[ProblemSpec.CAR_MOVE_RANGE];
        double kProbsSum = 0;
        double kProb;
        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProb = pCarGivenK[k]*pDriverGivenK[k]*
                    pTireGivenK[k]* priorK; //*pPressureTerrainGivenK[k];
            kProbsSum += kProb;
            kProbs[k] = kProb;
        }

        for (int k = 0; k < ProblemSpec.CAR_MOVE_RANGE; k++) {
            kProbs[k] /= kProbsSum;
        }

        float probability = 0;
        for(int i=0; i<kProbs.length-2; i++) {
            if(s.getPos() + (i-4) < 0) {
                probability += kProbs[i];
                kProbs[i] = 0;
            } else if (s.getPos() + (i-4) >= ps.getN()) {
                int maxSteps = ps.getN() - 1 - s.getPos();
                State s1 = s.copyState().changePosition(maxSteps, ps.getN());
                kProbs[maxSteps] += (kProbs[i]*valueIteration.get(s1));
                kProbs[i] = 0;
            } else {
                kProbs[i] += probability;
                State s1 = s.copyState().changePosition(i-4, ps.getN());
                kProbs[i] *= valueIteration.get(s1);
                probability = 0;
            }
        }
        kProbs[10] *= -ps.getSlipRecoveryTime()*0.1;
        kProbs[11] *= -ps.getRepairTime()*0.1;

        double sumTransistion = 0.0;
        for(Double prob : kProbs) {
            sumTransistion += prob;
        }

        return sumTransistion;
    }

    public void initReward() {
        State state;
        for(int pos=0; pos<ps.getN(); pos++) {
            for(int car=0; car<ps.getCT(); car++) {
                for(int driver=0; driver<ps.getDT(); driver++) {
                    for(int tire=0; tire<ps.NUM_TYRE_MODELS; tire++) {
                        if(ps.getLevel().getLevelNumber() == 1) {
                            state = new State(pos, false, false, cars.get(car),
                                    50, TirePressure.ONE_HUNDRED_PERCENT, drivers.get(driver), tires.get(tire));
                            if(pos != ps.getN()-1) {
                                valueIteration.put(state, -0.1);
                                reward.put(state, -0.1);
                            } else {
                                valueIteration.put(state, 10.0);
                                reward.put(state, 10.0);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Apply bayes rule to all values in cond probs list.
     *
     * @param condProb list of P(B|A)
     * @param priorA   prior probability of parameter A
     * @param priorB   prior probability of parameter B
     * @return list of P(A|B)
     */
    private double[] bayesRule(double[] condProb, double priorA, double priorB) {

        double[] swappedProb = new double[condProb.length];

        for (int i = 0; i < condProb.length; i++) {
            swappedProb[i] = (condProb[i] * priorA) / priorB;
        }
        return swappedProb;
    }
}

