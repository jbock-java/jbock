package net.jbock.util;

import net.jbock.model.CommandModel;

import java.nio.file.Path;

/**
 * Represents a syntax error in the {@code @file}.
 * This is an intermediate step in the construction of an
 * {@link ErrAtFile} instance.
 */
final class AtFileSyntaxError extends AtFileError {

    private final int number; // line number
    private final String message;

    AtFileSyntaxError(Path file, int number, String message) {
        super(file);
        this.number = number;
        this.message = message;
    }

    @Override
    public ParsingFailed addModel(CommandModel model) {
        return new ErrAtFile(model, file(), "at line " + number + ": " + message);
    }
}
