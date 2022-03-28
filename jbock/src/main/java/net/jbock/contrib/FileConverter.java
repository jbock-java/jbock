package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.io.File;
import java.nio.file.Paths;

/**
 * A {@code StringConverter} that converts the given path name to a {@code File}.
 * Fails if the input path does not exist.
 *
 * <p>Note: Use {@link java.nio.file.Path} for paths
 *    that may not exist.
 */
public final class FileConverter extends StringConverter<File> {

    FileConverter() {
    }

    @Override
    protected File convert(String token) {
        File file = Paths.get(token).toFile();
        if (!file.exists()) {
            throw new IllegalStateException("Path does not exist: " + token);
        }
        return file;
    }
}
