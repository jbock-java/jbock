package net.jbock.parse;

import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * Interface for a mutable class that reads and remembers option arguments.
 */
public interface OptionState {

    /**
     * Reads the argument of the {@code token}, or remembers the
     * {@code read} invocation, and stores this information internally.
     * The argument can be embedded in the token,
     * or it might be read from {@code it}.
     *
     * @param token an option name, or a combination of name and value
     * @param it an iterator, which might contain additional tokens
     * @return either {@code null} if the token was completely processed,
     *         or a reduced option group, if {@code token} is a group
     *         of more than one option
     * @throws ExToken if the input is not valid command line syntax
     */
    String read(String token, Iterator<String> it) throws ExToken;

    /**
     * Returns the internal state as a stream of strings.
     * To be invoked after parsing is finished.
     *
     * @return a stream of strings
     */
    Stream<String> stream();
}
