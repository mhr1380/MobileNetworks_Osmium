package com.example.myapplication.utils.RSSICalculation;

public class RssiCalculator {

    // Reference distance (typically 1 meter)
    private static final double REFERENCE_DISTANCE = 1.0;

    // Path loss at reference distance (d0), in dB (depends on the environment)
    private static final double PATH_LOSS_AT_REFERENCE_DISTANCE = 40.0;

    // Path loss exponent (depends on the environment)
    private static final double PATH_LOSS_EXPONENT = 4.0;

    /**
     * Calculate the distance based on the RSSI value.
     *
     * @param rssi the received signal strength indicator (in dBm)
     * @return the estimated distance (in meters)
     */
    public static double calculateDistance(double rssi) {
        return REFERENCE_DISTANCE * Math.pow(10, (PATH_LOSS_AT_REFERENCE_DISTANCE - rssi) / (10 * PATH_LOSS_EXPONENT));
    }
}
