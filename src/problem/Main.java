package problem;

import simulator.Policy;
import simulator.Simulator;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        ProblemSpec ps;
        try {
            ps = new ProblemSpec(args[0]); //args[0]);
            System.out.println(ps.toString());
            Policy p = new Policy(ps);
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");
    }
}
