package SariSariStore.util;  // declares where ung package naka tira so like pede siya magchange 

import java.text.NumberFormat;  // built in class for formattingb numbs, currency,percentage
import java.util.Locale; // controls like the currency and symbols ung locale represents region and language

public class MoneyFormatter {  // 

    private static final NumberFormat FORMATTER =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("en-PH"));

    private MoneyFormatter() {   // private naga prevent  new MoneyFormatter()
    // utility class 
    }

    public static String format(double amount) {  // naga convert ng number into a peso-formatted string
        return FORMATTER.format(amount);
    }
}
