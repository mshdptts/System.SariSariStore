package SariSariStore.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Formats monetary values using the Philippine peso currency format. */
public class MoneyFormatter {

    private static final NumberFormat FORMATTER =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("en-PH"));

    private MoneyFormatter() {
        // Utility class.
    }

    /** Converts an amount to a peso-formatted string. */
    public static String format(double amount) {
        return FORMATTER.format(amount);
    }
}
