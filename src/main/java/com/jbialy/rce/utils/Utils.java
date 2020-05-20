package com.jbialy.rce.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class Utils {
    private static final long KIBI = 1024L;
    private static final long MEBI = KIBI * 1024L;
    private static final long GIBI = MEBI * 1024L;
    private static final long TEBI = GIBI * 1024L;

    private static final long KILO = 1000L;
    private static final long MEGA = KILO * 1000L;
    private static final long GIGA = MEGA * 1000L;
    private static final long TERA = GIGA * 1000L;

    private Utils() {
    }

    public static double round(double value, int places) {
        if (places < 0) {
            places = 0;
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static byte[] zipCompress(byte[] data) throws IOException {
        return new DeflaterInputStream(new ByteArrayInputStream(data)).readAllBytes();
    }

    public static byte[] zipDecompress(byte[] data) throws IOException {
        return new InflaterInputStream(new ByteArrayInputStream(data)).readAllBytes();
    }

    public static boolean isValidZip(byte[] data) {
        try {
            new InflaterInputStream(new ByteArrayInputStream(data)).readAllBytes();
        } catch (ZipException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static String toHumanReadableBinarySize(long length, int precision) {
        String unit;
        String value;

        if (length < KIBI) {
            value = String.valueOf(length);
            unit = "";
        } else if (length < MEBI) {
            value = doubleToString(round((double) length / KIBI, precision), precision, true);
            unit = "Ki";
        } else if (length < GIBI) {
            value = doubleToString(round((double) length / MEBI, precision), precision, true);
            unit = "Mi";
        } else if (length < TEBI) {
            value = doubleToString(round((double) length / GIBI, precision), precision, true);
            unit = "Gi";
        } else {
            value = doubleToString(round((double) length / TEBI, precision), precision, true);
            unit = "Ti";
        }

        return value + unit;
    }

    public static String toHumanReadableMetricSize(long inputValue, int precision) {
        return toHumanReadableMetricSize(inputValue, "", precision);
    }

    public static String toHumanReadableMetricSize(long inputValue, String unitSeparator, int precision) {
        String unit;
        String value;

        if (inputValue < KILO) {
            value = String.valueOf(inputValue);
            unit = "";
            unitSeparator = "";
        } else if (inputValue < MEGA) {
            value = doubleToString(round((double) inputValue / KILO, precision), precision, true);
            unit = "K";
        } else if (inputValue < GIGA) {
            value = doubleToString(round((double) inputValue / MEGA, precision), precision, true);
            unit = "M";
        } else if (inputValue < TERA) {
            value = doubleToString(round((double) inputValue / GIGA, precision), precision, true);
            unit = "G";
        } else {
            value = doubleToString(round((double) inputValue / TERA, precision), precision, true);
            unit = "T";
        }

        return value + unitSeparator + unit;
    }

    public static String toHumanReadableMetricSize(double doubleInputValue, String unitSeparator, int precision) {
        String unit;
        String value;

        if (doubleInputValue < KILO) {
            value = doubleToString(round(doubleInputValue, precision), precision, true);
            unit = "";
            unitSeparator = "";
        } else if (doubleInputValue < MEGA) {
            value = doubleToString(round(doubleInputValue / KILO, precision), precision, true);
            unit = "K";
        } else if (doubleInputValue < GIGA) {
            value = doubleToString(round(doubleInputValue / MEGA, precision), precision, true);
            unit = "M";
        } else if (doubleInputValue < TERA) {
            value = doubleToString(round(doubleInputValue / GIGA, precision), precision, true);
            unit = "G";
        } else {
            value = doubleToString(round(doubleInputValue / TERA, precision), precision, true);
            unit = "T";
        }

        return value + unitSeparator + unit;
    }

    public static String doubleToString(double d, int precision, boolean appendZeros) {
        double rounded = round(d, precision);
        String roundedString = String.valueOf(rounded);

        if (appendZeros) {
            int currentStringPrecision = roundedString.substring(roundedString.lastIndexOf('.') + 1).length();

            if (currentStringPrecision < precision) {
                StringBuilder sb = new StringBuilder(roundedString);

                for (int i = currentStringPrecision; i < precision; i++) {
                    sb.append("0");
                }

                return sb.toString();
            }
        }

        return roundedString;
    }

    public static <T> void shuffleArray(T[] inputArray) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = inputArray.length - 1; i > 0; i--) {
            int indexToSwap = random.nextInt(i + 1);

            T tmp = inputArray[i];
            inputArray[i] = inputArray[indexToSwap];
            inputArray[indexToSwap] = tmp;
        }
    }

    public static void shuffleIntArray(int[] inputArray) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = inputArray.length - 1; i > 0; i--) {
            int indexToSwap = random.nextInt(i + 1);

            int tmp = inputArray[i];
            inputArray[i] = inputArray[indexToSwap];
            inputArray[indexToSwap] = tmp;
        }
    }

}
