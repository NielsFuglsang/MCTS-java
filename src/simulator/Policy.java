package simulator;

import problem.ActionType;
import problem.ProblemSpec;

import java.util.HashMap;

public class Policy {
    private ValueIteration vi;
    private ProblemSpec ps;

    public Policy(ProblemSpec ps) {
        this.ps = ps;
        vi = new ValueIteration(ps);
        vi.initReward();
        vi.valueIteration();
    }

    public ActionType chooseAction(State state) { //Must return the parameters for the action as well!
        vi.initReward();
        vi.valueIteration();
        HashMap<State, Double> val = vi.valueIteration;

        for(ActionType actionType : ps.getLevel().getAvailableActions()) {
            if(actionType.getActionNo() == 1) {

            } else if(actionType.getActionNo() == 2) {

            }
        }

        return null;

    }
}