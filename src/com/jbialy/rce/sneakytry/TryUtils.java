package com.jbialy.rce.sneakytry;

import java.time.Duration;
import java.util.function.Predicate;

public class TryUtils {
    private TryUtils() {
    }

    public static <T, E extends Throwable> T multiTry(ThrowingSupplier<T, E> supplier, long maxTries) throws E {
        return multiTry(supplier, maxTries, Duration.ofSeconds(0L));
    }

    public static <T, E extends Throwable> T multiTry(ThrowingSupplier<T, E> supplier, long maxTries, Duration delay) throws E {
        boolean success = false;
        T result = null;
        long triesCounter = 0L;

        do {
            try {
                result = supplier.get();
                success = true;
            } catch (Throwable t) {
                triesCounter++;

                if (maxTries <= triesCounter) {
                    throw (E) t;
                }

                sleep(delay);
            }
        } while (!success);

        return result;
    }

    public static <T, E extends Throwable> T multiTry(ThrowingSupplier<T, E> supplier, long maxTries, Duration delay, Predicate<E> repetitionCondition) throws E {
        boolean success = false;
        T result = null;
        long triesCounter = 0L;

        do {
            try {
                result = supplier.get();
                success = true;
            } catch (Throwable t) {
                triesCounter++;

                if (repetitionCondition.test((E) t) || maxTries < triesCounter) {
                    throw (E) t;
                }

                sleep(delay);
            }
        } while (!success);

        return result;
    }

    public static <T, E extends Throwable> T multiTry(ThrowingOperation<E> operation, long maxTries, Duration delay, Predicate<Exception> repetitionCondition) throws E {
        boolean success = false;
        T result = null;
        long triesCounter = 0L;

        do {
            if (triesCounter > 0 && delay.toMillis() > 0) {
                sleep(delay);
            }

            try {
                operation.make();
                success = true;
            } catch (Exception t) {
                triesCounter++;

                if (repetitionCondition.test(t) || maxTries <= triesCounter) {
                    throw (E) t;
                }
            }
        } while (!success);

        return result;
    }

    public static <E extends Throwable> void multiTry(ThrowingOperation<E> operation, long maxTries) throws E {
        multiTry(operation, maxTries, Duration.ofMillis(0L));
    }

    public static <E extends Throwable> void multiTry(ThrowingOperation<E> operation, long maxTries, Duration delay) throws E {
        boolean success = false;
        long triesCounter = 0L;

        do {
            try {
                operation.make();
                success = true;
            } catch (Exception t) {
                triesCounter++;

                if (maxTries <= triesCounter) {
                    throw (E) t;
                }

                sleep(delay);
            }
        } while (!success);
    }

    public static <T, E extends Throwable> T sneakyMultiTry(ThrowingSupplier<T, E> supplier, long maxTries, T returnWhenThrow) {
        boolean success = false;
        T result = null;
        long triesCounter = 0L;

        do {
            try {
                result = supplier.get();
                success = true;
            } catch (Throwable t) {
                triesCounter++;

                if (triesCounter < maxTries) {
                    return returnWhenThrow;
                }
            }
        } while (!success);

        return result;
    }

    public static <T, E extends Throwable> T sneakyTry(ThrowingSupplier<T, E> supplier, T returnWhenThrow) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return returnWhenThrow;
        }
    }

    public static <E extends Throwable> void sneakyTry(ThrowingOperation<E> operation) {
        try {
            operation.make();
        } catch (Throwable ignored) {

        }
    }

    private static void sleep(Duration duration) {
        if (duration.toMillis() > 0L) {
            try {
                Thread.sleep(duration.toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}
