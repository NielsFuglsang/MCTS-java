package simulator;

import problem.Action;
import problem.ProblemSpec;
import simulator.State;

import java.util.HashMap;
import java.util.LinkedList;

public class ValueIteration {
    ProblemSpec ps;

    public void ValueIteration() {
        /* for(int i=0; i<ps.getN(); i++) {
            for(int j=0; j<ps.getCT(); j++) {
                for(int k=0; k<ps.getDT(); k++) {
                    for(int m=0; m<ps.NUM_TYRE_MODELS; m++) {
                        State state = new State(i, )
                    }
                }
            }
        }*/


    }

    public float[] transistionFunc(State s) {
        float[] transistionFunction = new float[ps.CAR_MOVE_RANGE];

        float priorK = 1 / ps.CAR_MOVE_RANGE;
        float priorCar = 1 / ps.getCT();
        float priorDriver = 1 / ps.getDT();
        float priorTire = 1 / ps.NUM_TYRE_MODELS;
        // get probabilities of k given parameter
        float[] pKGivenCar = ps.getCarMoveProbability().get(s.getCarType());
        float[] pKGivenDriver = ps.getDriverMoveProbability().get(s.getDriver());
        float[] pKGivenTire = ps.getTireModelMoveProbability().get(s.getTireModel());

        // use bayes rule to get probability of parameter given k
        float[] pCarGivenK = bayesRule(pKGivenCar, priorCar, priorK);
        float[] pDriverGivenK = bayesRule(pKGivenDriver, priorDriver, priorK);
        float[] pTireGivenK = bayesRule(pKGivenTire, priorTire, priorK);

        int[] rewards = reward(s);
        float sum = 0;
        for (int k=0; k<ps.CAR_MOVE_RANGE; k++) {
            transistionFunction[k] = pCarGivenK[k]*pDriverGivenK[k]*pTireGivenK[k]*priorK;
            sum += transistionFunction[k];
        }
        for(int i=0; i<transistionFunction.length; i++) {
            transistionFunction[i] = (transistionFunction[i]/sum);
        }

        float probability = 0;
        for(int i=0; i<transistionFunction.length; i++) {
            if(s.getPos() +(i-4) < 0) {
                probability += transistionFunction[i];
                transistionFunction[i] = 0;
            } else if (s.getPos() + (i-4) >= ps.getN()) {
                int maxSteps = ps.getN() - 1 - s.getPos();
                transistionFunction[maxSteps] += (transistionFunction[i]*rewards[maxSteps]);
                transistionFunction[i] = 0;
            } else {
                transistionFunction[i] += probability;
                transistionFunction[i] *= rewards[i];
                probability = 0;
            }
        }

        return transistionFunction;
    }

    private int[] reward(State current) {
        int[] rewards = new int[ps.getN()];
        int reward = 0;
        for(int i=current.getPos(); i>=0; i++) {
            rewards[i] = reward;
            reward--;
        }
        reward = 0;
        for(int i=current.getPos(); i<ps.getN(); i++) {
            rewards[i] = reward;
            reward++;
        }
        rewards[ps.getN()-1] = 50;
        return rewards;
    }


    /**
     * Apply bayes rule to all values in cond probs list.
     *
     * @param condProb list of P(B|A)
     * @param priorA   prior probability of parameter A
     * @param priorB   prior probability of parameter B
     * @return list of P(A|B)
     */
    private float[] bayesRule(float[] condProb, float priorA, float priorB) {

        float[] swappedProb = new float[condProb.length];

        for (int i = 0; i < condProb.length; i++) {
            swappedProb[i] = (condProb[i] * priorA) / priorB;
        }
        return swappedProb;
    }
}

