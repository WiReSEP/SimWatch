package de.tu_bs.wire.simwatch.api;


import java.io.File;
import java.util.Random;

public class Demo {

    private static final int SIZE = 20;

    public static void main(String[] args) {
        ApiConnector apiConnector = ApiConnector.getInstance();
        apiConnector.register("My Simulation #1", new File("example_profile.json"));
        try {
            Random rand = new Random();
            int[] numbers = new int[SIZE];
            for (int i = 0; i < SIZE; i++) {
                numbers[i] = rand.nextInt();
                apiConnector.buildUpdate()
                        .put("finished", false)
                        .put("progress", (i + 1) / (double) SIZE)
                        .put("numbers", numbers)
                        .post();
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
