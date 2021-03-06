package de.tu_bs.wire.simwatch.api;


import de.tu_bs.wire.simwatch.api.types.Vector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

/**
 * API usage example. Waits for enter press between updates. Demonstrates usage
 * of updates with images, vectors and plottable data.
 */
class Demo {
    private static final int SIZE = 20;

    public static void main(String[] args) {
        try {
            final SimWatchClient client = SimWatchClient.registerSimulation("My Simulation #1",
                    new File("example_profile.json"));
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    client.notifyFailed(throwable);
                }
            });

            Random rand = new Random();
            Scanner scanner = new Scanner(System.in);
            final BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            Integer[] numbers = new Integer[SIZE];

            for (int i = 0; i < SIZE; i++) {
                graphics.drawString("Hello world " + i,
                        rand.nextInt(image.getWidth()), rand.nextInt(image.getHeight()));
                numbers[i] = rand.nextInt();
                client.buildUpdate()
                        .put("iteration", i)
                        .put("result", 1 - Math.pow(1.1, -2 * i))
                        .put("progress", (i + 1) / (double) SIZE)
                        .put("finished", i + 1 == SIZE)
                        .put("numbers", new Vector(Arrays.copyOfRange(numbers, 0, i + 1)))
                        .attach("render", new AttachmentStreamer() {
                            @Override
                            public void writeTo(OutputStream outputStream) throws IOException {
                                ImageIO.write(image, "png", outputStream);
                            }
                        })
                        .post();
                System.out.println("Press [ENTER] ");
                String line = scanner.nextLine();
                if (!line.isEmpty()) {
                    if (line.equals("crash")) {
                        throw new RuntimeException("User requested a crash");
                    }
                    client.notifyFailed(line);
                    return;
                }
            }

            client.notifyDone();

            Thread.setDefaultUncaughtExceptionHandler(null);
        } catch (RegistrationException e) {
            e.printStackTrace();
        }
    }
}
