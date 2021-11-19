package net.jbock.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SnakeName {

    private final List<String> parts; // lower case parts

    private SnakeName(List<String> parts) {
        this.parts = parts;
    }

    public static SnakeName create(CharSequence input) {
        List<String> result = new ArrayList<>();
        CharType t2 = CharType.OTHER;
        CharType t1 = CharType.OTHER;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            CharType t0 = charType(input.charAt(i));
            boolean typeChange = t0 != CharType.OTHER
                    && t1 != CharType.OTHER
                    && t1 == t2
                    && t0 != t1;
            boolean caseUp = t1 == CharType.LOWER
                    && t0 == CharType.UPPER;
            if (sb.length() > 0 && (caseUp || typeChange)) {
                result.add(sb.toString().toLowerCase(Locale.US));
                sb.setLength(0);
            }
            sb.append(input.charAt(i));
            t2 = t1;
            t1 = t0;
        }
        if (sb.length() > 0) {
            result.add(sb.toString().toLowerCase(Locale.US));
        }
        return new SnakeName(result);
    }

    public String snake(char delim) {
        return String.join(Character.toString(delim), parts);
    }

    private enum CharType {
        LOWER, UPPER, DIGIT, OTHER
    }

    private static CharType charType(char c) {
        if (Character.isLowerCase(c)) {
            return CharType.LOWER;
        }
        if (Character.isUpperCase(c)) {
            return CharType.UPPER;
        }
        if (Character.isDigit(c)) {
            return CharType.DIGIT;
        }
        return CharType.OTHER;
    }
}
