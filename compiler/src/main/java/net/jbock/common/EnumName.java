package net.jbock.common;

import java.util.Locale;
import java.util.regex.Pattern;

public class EnumName {

    private static final Pattern ENDS_WITH_NUMBER = Pattern.compile(".*\\d");
    private final String enumConstant; // all-caps, unique
    private final String original; // case-independently unique

    private EnumName(String original, String enumConstant) {
        this.original = original;
        this.enumConstant = enumConstant;
    }

    public static EnumName create(String input) {
        if ("_".equals(input)) {
            return new EnumName("__", "__"); // prevent potential problems
        }
        return new EnumName(input, input.toUpperCase(Locale.US));
    }

    public EnumName makeLonger() {
        String appendage = ENDS_WITH_NUMBER.matcher(enumConstant).matches() ? "1" : "_1";
        return new EnumName(original + appendage, enumConstant + appendage);
    }

    /* All-caps, unique.
     */
    public String enumConstant() {
        return enumConstant;
    }

    /* Case-independently unique.
     */
    public String original() {
        return original;
    }
}
