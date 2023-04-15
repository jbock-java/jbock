package net.jbock.util;

import java.util.List;

/**
 * Success result of parsing a SuperCommand.
 *
 * @param <S> type of the SuperCommand
 */
public final class SuperResult<S> {

    private final S superCommand;
    private final List<String> rest;

    /**
     * Constructor to be used by generated code.
     *
     * @param superCommand command instance
     * @param rest remaining tokens ignored by the SuperCommand,
     *             to be used by a subcommand
     */
    public SuperResult(S superCommand, List<String> rest) {
        this.superCommand = superCommand;
        this.rest = rest;
    }

    /**
     * Returns the command instance.
     *
     * @return an instance of the SuperCommand type
     */
    public S getCommand() {
        return superCommand;
    }


    /**
     * Returns the remaining tokens, after the SuperCommand's last parameter.
     *
     * @return remaining tokens, suitable for further parsing
     */
    public List<String> rest() {
        return rest;
    }

    /**
     * Returns the remaining tokens, after the SuperCommand's last parameter.
     *
     * @return remaining tokens, suitable for further parsing
     */
    public String[] getRest() {
        return rest.toArray(new String[0]);
    }
}
