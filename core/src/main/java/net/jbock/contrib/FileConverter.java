package net.jbock.contrib;

import net.jbock.util.StringConverter;

import java.io.File;

public final class FileConverter extends StringConverter<File> {

    private FileConverter() {
    }

    public static FileConverter create() {
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
