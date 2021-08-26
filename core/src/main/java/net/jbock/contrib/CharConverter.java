package net.jbock.contrib;

import net.jbock.util.StringConverter;

public final class CharConverter extends StringConverter<Character> {

    private CharConverter() {
    }

    public static CharConverter create() {
        return new CharConverter();
    }

    @Override
    protected Character convert(String token) {
        if (token.length() != 1) {
            throw new RuntimeException("Not a single character: <" + token + ">");
        }
        return token.charAt(0);
    }
}
