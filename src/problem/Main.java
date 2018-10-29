package problem;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        ProblemSpec ps;
        try {
            ps = new ProblemSpec(args[0]);
            System.out.println(ps.toString());
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");

    }
}
