package vn.io.huangnosimp.Manager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public class FormatUtil {
    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        NUMBER_FORMAT = new DecimalFormat("#,###.##", symbols);
    }

    // Gọi ở bất kỳ đâu trong dự án
    public static String formatNumber(double number) {
        return NUMBER_FORMAT.format(number);
    }

    public static double parseNumber(String formatted) {
        if (formatted == null || formatted.trim().isEmpty()) return 0;
        try {
            return NUMBER_FORMAT.parse(formatted).doubleValue();
        } catch (ParseException e) {
            System.out.println("Chuỗi không đúng định dạng: " + formatted);
            return 0;
        }
    }
}

