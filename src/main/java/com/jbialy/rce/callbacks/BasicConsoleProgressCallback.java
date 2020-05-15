package com.jbialy.rce.callbacks;

import com.jbialy.rce.DataCallback;
import com.jbialy.rce.JobStatistics;
import com.jbialy.rce.utils.Utils;

public class BasicConsoleProgressCallback implements DataCallback<JobStatistics> {
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private long startTime = 0L;
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private long itemStart = 0L;
    private volatile boolean firstCall = true;

    public static void printProgress(double percentage, int width) {
        char[] c = {'░', '▒', '▓', '█'};
        double[] charWidths = {0.25, 0.50, 0.75, 1.0};
//        char[] c = {'⠂', '⠒', '⠇', '⠗', '⠯', '⠿'};
//        double[] charWidths = {1.0 / 6, 2.0 / 6, 3.0 / 6, 4.0 / 6, 5.0 / 6};
        //          7/8   6/8  5/8  3/8  2/8  1/8
//        char[] c = {'▏', '▎', '▍', '▋', '▊', '▉'};
//        double[] charWidths = {1.0 / 8, 2.0 / 8, 3.0 / 8, 5.0 / 8, 6.0 / 8, 7.0 / 8};
//        char[] c = {'▉', '▊', '▋', '▍', '▎', '▏'};
//        double[] charWidths = {7.0 / 8, 6.0 / 8, 5.0 / 8, 3.0 / 8, 2.0 / 8, 1.0 / 8};
//        int width = 25;
//        double percentage = 0.5625;

        int fullBlocks = (int) (percentage / (1.0 / width));
        int emptyBlocks = (int) ((1 - percentage) / (1.0 / width));

        for (int i = 0; i < fullBlocks; i++) { //Print full boxes
            System.out.print(c[c.length - 1]);
        }

        boolean printIntermediate = width - fullBlocks - emptyBlocks > 0;
        if (printIntermediate) {
            double a = percentage - ((1.0 / width) * fullBlocks);
            double l = 1.0 / width;
            double intermediateBlockPercentage = a / l;
            int index = findBestWidth(intermediateBlockPercentage, charWidths);
            System.out.print(c[index]);
        }

        for (int i = 0; i < emptyBlocks; i++) { //Print full boxes
//            System.out.print('\u3000');
            System.out.print(" ");
//            System.out.print('\u2840');
        }
    }

    public static String toStringProgress(double percentage, int width) {
        String result = "";

        char[] c = {'░', '▒', '▓', '█'};
        double[] charWidths = {0.25, 0.50, 0.75, 1.0};

        int fullBlocks = (int) (percentage / (1.0 / width));
        int emptyBlocks = (int) ((1 - percentage) / (1.0 / width));

        for (int i = 0; i < fullBlocks; i++) { //Print full boxes
            result += "" + c[c.length - 1];
        }

        boolean printIntermediate = width - fullBlocks - emptyBlocks > 0;
        if (printIntermediate) {
            double a = percentage - ((1.0 / width) * fullBlocks);
            double l = 1.0 / width;
            double intermediateBlockPercentage = a / l;
            int index = findBestWidth(intermediateBlockPercentage, charWidths);
            result += c[index];
        }

        for (int i = 0; i < emptyBlocks; i++) { //Print full boxes
            result += " ";
        }

        return result;
    }

    private static int findBestWidth(double percentage, double[] widths) {
        for (int i = 0; i < widths.length - 1; i++) {
            if (percentage <= widths[i + 1]) {
                return i;
            }
        }

        return widths.length - 1;
    }

    private void init(JobStatistics stats) {
        startTime = System.currentTimeMillis();
        itemStart = stats.getDoneTasks();
        firstCall = false;
    }

    @Override
    public void onCall(JobStatistics stats) {
        if (firstCall) {
            synchronized (this) {
                if (firstCall) {
                    init(stats);
                    return;
                }
            }
        }

        double dt = (System.currentTimeMillis() - startTime) / 1000.0;
        double avgSpd = (stats.getDoneTasks() - itemStart) / dt;

        final double doublePercentage = 100 * stats.getDoneTasks() / (double) stats.getAllTasksCount();
        String percentage = Utils.toHumanReadableMetricSize(doublePercentage, "", 2);
//        System.out.println(stats + " avgSpd: " + Utils.toHumanReadableMetricSize(avgSpd, "", 2) + " | " + percentage + " %");
        System.out.println(
                stats +
                        " avgSpd: " +
                        Utils.toHumanReadableMetricSize(avgSpd, "", 2) + " | " +
                        toStringProgress(doublePercentage / 100.0, 16) +
                        "| " +
                        percentage + " %");
    }
}
