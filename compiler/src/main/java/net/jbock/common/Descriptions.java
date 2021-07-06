package net.jbock.common;

import java.util.Optional;

public class Descriptions {

    public static Optional<String> optionalString(String s) {
        if (s.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(s);
    }
}
