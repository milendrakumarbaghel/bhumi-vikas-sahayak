package org.springboot.backend.calc;

import java.util.Locale;

/** Unit conversions. Everything inside the app is in metres and square metres. */
public final class Units {

    public static final double SQFT_PER_SQM = 10.763910416709722; // 1 m = 3.28084 ft, squared
    public static final double SQM_PER_SQYD = 0.83612736;          // 1 yd = 0.9144 m, squared
    public static final double M_PER_FT = 0.3048;

    private Units() {
    }

    /** Converts an area to square metres. Unit: "sqm", "sqft" or "sqyd" (gaj). */
    public static double areaToSqm(double value, String unit) {
        return switch (unit == null ? "sqm" : unit.toLowerCase(Locale.ROOT)) {
            case "sqft" -> value / SQFT_PER_SQM;
            case "sqyd" -> value * SQM_PER_SQYD;
            default -> value;
        };
    }

    /** Converts a length to metres. Unit: "m" or "ft". */
    public static double lengthToM(double value, String unit) {
        return "ft".equalsIgnoreCase(unit) ? value * M_PER_FT : value;
    }

    public static double sqmToSqft(double sqm) {
        return sqm * SQFT_PER_SQM;
    }
}
