package com.energyxxer.guardian.util;

/**
 * Created by User on 2/22/2017.
 */
public class MathUtil {
    public static double truncateDecimals(double n, int precision) {
        return Math.floor(n * Math.pow(10, precision)) / (Math.pow(10, precision));
    }
}
