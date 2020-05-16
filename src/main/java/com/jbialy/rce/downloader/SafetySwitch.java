package com.jbialy.rce.downloader;

import java.util.function.Predicate;

public class SafetySwitch<T> {
    private final Predicate<T> predicate;
    private volatile boolean activated = false;

    public SafetySwitch(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    public boolean isActivated() {
        if (!this.activated) {
            synchronized (this) {
                return this.activated;
            }
        }
        return true;
    }

    public boolean safeTestPass(T input) {
        if (this.activated) { //Try avoid synchronization
            return false;
        } else {
            synchronized (this) {
                if (!this.activated) {
                    if (this.predicate.test(input)) {
                        this.activated = true;
                        return false;
                    }
                }
            }

            return true;
        }
    }

//    public void test(T input) {
//        if (!this.activated) { //Try avoid synchronization
//            synchronized (this) {
//                if (!this.activated) {
//                    if (this.predicate.test(input)) {
//                        this.activated = true;
//                    }
//                }
//            }
//        }
//    }

//    public void test(T input) {
//        if (!this.activated) { //Try avoid synchronization
//            synchronized (this) {
//                if (!this.activated) {
//                    if (this.predicate.test(input)) {
//                        this.activated = true;
//                    }
//                }
//            }
//        }
//    }

    public T testAndReturn(T input) {
        if (!this.activated) { //Try avoid synchronization
            synchronized (this) {
                if (!this.activated) {
                    if (this.predicate.test(input)) {
                        this.activated = true;
                    }
                }
            }
        }

        return input;
    }


    public void activate() {
        if (!this.activated) {
            synchronized (this) {
                this.activated = true;
            }
        }
    }
}
