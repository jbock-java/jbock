package net.jbock.contrib;

import net.jbock.util.Either;

import java.io.File;
import java.nio.file.Paths;

import static net.jbock.util.Either.left;
import static net.jbock.util.Either.right;

final class MoreConverters {

    static Either<String, File> existingFile(String token) {
        File file = Paths.get(token).toFile();
        if (!file.exists()) {
            return left("Path does not exist: " + token);
        }
        return right(file);
    }

    static Either<String, Character> asCharacter(String token) {
        if (token.isEmpty()) {
            return left("Expecting a single character, but found an empty string");
        }
        if (token.length() >= 2) {
            return left("Expecting a single character, but found: <" + token + ">");
        }
        return right(token.charAt(0));
    }

    private MoreConverters() {
    }
}
