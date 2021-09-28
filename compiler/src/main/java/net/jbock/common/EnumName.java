package net.jbock.common;

import java.util.Locale;

public class EnumName {

    private final String enumConstant; // all-caps, unique

    private EnumName(String enumConstant) {
        this.enumConstant = enumConstant;
    }

    public static EnumName create(String input) {
        if ("_".equals(input)) {
            return new EnumName("_1"); // prevent potential problems
        }
        String snakeName = SnakeName.create(input).snake('_');
        return new EnumName(snakeName.toUpperCase(Locale.US));
    }

    public EnumName makeLonger() {
        String suffix = enumConstant.endsWith("1") ? "1" : "_1";
        return new EnumName(enumConstant + suffix);
    }

    /* All-caps, unique.
     */
    public String enumConstant() {
        return enumConstant;
    }
}
