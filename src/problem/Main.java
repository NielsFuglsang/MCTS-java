package problem;

import java.io.IOException;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        ProblemSpec ps;
        try {
            ps = new ProblemSpec("examples\\level_1\\input1.txt");
            System.out.println(ps.toString());
        } catch (IOException e) {
            System.out.println("IO Exception occurred");
            System.exit(1);
        }
        System.out.println("Finished loading!");

    }
}
