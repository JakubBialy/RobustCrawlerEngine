package com.jbialy.rce.jvm.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeTest {

    @Test()
    public void duplicatedTimestamps() {
        Assertions.assertThrows(Exception.class, () -> {
            long lastTimestamp = 0L;
            long counter = 0;
            for (int i = 0; i < 0x7fffffff; i++) {
                long currentTimestamp = System.nanoTime();

                if (currentTimestamp == lastTimestamp) {
                    System.out.println("Counter: " + counter);
                    throw new Exception("Duplicated timestamps.");
                }

                lastTimestamp = currentTimestamp;
                counter++;
            }

            System.out.println(counter);
        });
    }
}
