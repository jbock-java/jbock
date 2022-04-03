package net.jbock;

/**
 * Success result of parsing a SuperCommand.
 *
 * @param <S> type of the SuperCommand
 */
public final class SuperCommandResult<S> {

    private final S superCommand;
    private final String[] rest;

    /**
     * Constructor that will be used by the generated code.
     *
     * @param superCommand command instance
     * @param rest input for the subcommand
     */
    public SuperCommandResult(S superCommand, String[] rest) {
        this.superCommand = superCommand;
        this.rest = rest;
    }

    /**
     * Returns the command instance.
     *
     * @return an instance of the SuperCommand
     */
    public S getCommand() {
        return superCommand;
    }

    /**
     * Returns the remaining tokens, after the SuperCommand's last parameter.
     *
     * @return remaining tokens, suitable for further parsing
     */
    public String[] getRest() {
        return rest;
    }
}
