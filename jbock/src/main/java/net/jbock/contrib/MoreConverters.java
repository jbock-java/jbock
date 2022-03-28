package net.jbock.contrib;

import java.io.File;
import java.nio.file.Paths;

final class MoreConverters {

    static File existingFile(String token) {
        File file = Paths.get(token).toFile();
        if (!file.exists()) {
            throw new IllegalStateException("Path does not exist: " + token);
        }
        return file;
    }

    static Character asCharacter(String token) {
        if (token.isEmpty()) {
            throw new RuntimeException("Expecting a single character, but found an empty string");
        }
        if (token.length() >= 2) {
            throw new RuntimeException("Expecting a single character, but found: <" + token + ">");
        }
        return token.charAt(0);
    }

    private MoreConverters() {
    }
}
