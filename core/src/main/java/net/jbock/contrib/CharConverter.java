package net.jbock.contrib;

import net.jbock.util.StringConverter;

/**
 * A {@code StringConverter} that converts to Character.
 * Fails if the input consists of more than one character.
 */
public final class CharConverter extends StringConverter<Character> {

    private CharConverter() {
    }

    /**
     * Creates an instance of {@code CharConverter}.
     * @return an instance of {@code CharConverter}
     */
    public static StringConverter<Character> create() {
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
