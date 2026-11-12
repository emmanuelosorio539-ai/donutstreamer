package com.donutstreamer;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Convierte entre texto tipo "1.5m", "250k", "$1,200" y números. */
public final class Money {
    private static final Pattern P = Pattern.compile("^\\$?\\s*([0-9][0-9.,]*)\\s*([kKmMbB]?)$");

    private Money() {}

    public static Double parse(String s) {
        if (s == null) return null;
        Matcher m = P.matcher(s.trim());
        if (!m.matches()) return null;
        try {
            double v = Double.parseDouble(m.group(1).replace(",", ""));
            switch (m.group(2).toLowerCase(Locale.ROOT)) {
                case "k": v *= 1_000d; break;
                case "m": v *= 1_000_000d; break;
                case "b": v *= 1_000_000_000d; break;
                default: break;
            }
            return v;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String format(double v) {
        double a = Math.abs(v);
        String sign = v < 0 ? "-" : "";
        if (a >= 1e9) return sign + "$" + trim(a / 1e9) + "B";
        if (a >= 1e6) return sign + "$" + trim(a / 1e6) + "M";
        if (a >= 1e3) return sign + "$" + trim(a / 1e3) + "K";
        return sign + "$" + trim(a);
    }

    private static String trim(double d) {
        String s = String.format(Locale.US, "%.2f", d);
        if (s.contains(".")) {
            s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s;
    }
}
