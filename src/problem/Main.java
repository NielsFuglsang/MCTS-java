package problem;

import simulator.Policy;
import simulator.Simulator;

import java.io.IOException;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
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
