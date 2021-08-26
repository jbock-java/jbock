package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.io.File;

/**
 * A {@code StringConverter} that converts to {@code File}.
 * Fails if the input file does not exist.
 *
 * <p>Note: Use {@link java.nio.file.Path} for file arguments
 *    that may not exist.
 */
public final class FileConverter extends StringConverter<File> {

    private FileConverter() {
    }

    /**
     * Creates an instance of {@code FileConverter}.
     * @return an instance of {@code FileConverter}
     */
    public static StringConverter<File> create() {
        return new FileConverter();
    }

    @Override
    protected File convert(String token) {
        File file = new File(token);
        if (!file.exists()) {
            throw new IllegalStateException("File does not exist: " + token);
        }
        if (!file.isFile()) {
            throw new IllegalStateException("Not a file: " + token);
        }
        return file;
    }
}
