package net.jbock.contrib;

import net.jbock.util.StringConverter;

/**
 * A {@code StringConverter} that converts to Character.
 * Fails if the input consists of more than one character.
 */
public final class CharConverter extends StringConverter<Character> {

    CharConverter() {
    }

    @Override
    protected Character convert(String token) {
        if (token.isEmpty()) {
            throw new RuntimeException("Expecting a single character, but found an empty string");
        }
        if (token.length() >= 2) {
            throw new RuntimeException("Expecting a single character, but found: <" + token + ">");
        }
        return token.charAt(0);
    }
}
