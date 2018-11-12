package problem;


import mcts.MCTS;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        ProblemSpec ps;
        long startTime = System.currentTimeMillis();
        int step = 1;
        try {
            ps = new ProblemSpec(args[0]); //args[0]);
            // System.out.println(ps.toString());
            MCTS mcts = new MCTS(ps, args[1]);
            step = mcts.stepCounter;
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        long endTime = System.currentTimeMillis();
        double duration = (double) (endTime - startTime) / 1000;
        System.out.println("Output created in " + duration + " seconds");
        System.out.println("Duration per step: " + (float) duration / step);
    }
}
