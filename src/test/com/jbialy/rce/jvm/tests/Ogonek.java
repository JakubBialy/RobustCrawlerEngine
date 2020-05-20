package com.jbialy.rce.jvm.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Ogonek {
    @Test()
    public void combiningOgonek() {
        Assertions.assertNotEquals("ą", "ą");
    }

//    @Test()
//    public void show() {
//        System.out.println("\u0105"); // ą
//        System.out.println("\u0061\u0328"); // a + ogonek
//        System.out.println("\u0105\u0328"); // ą + ogonek
//    }
}
