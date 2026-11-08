package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static net.jbock.util.ErrTokenType.INVALID_OPTION;

/**
 * Abstract superclass of several primitive command line parsers.
 * These parsers are mutable and not thread safe.
 */
public abstract sealed class AbstractParser
        permits StandardParser, SuperParser, VarargsParameterParser {

    private static final Pattern SUSPICIOUS = Pattern.compile("^-\\S.*");

    private final Map<String, Integer> optionNames;
    private final OptionState[] optionStates;
    private final String[] params;

    AbstractParser(
            Map<String, Integer> optionNames,
            OptionState[] optionStates,
            int numParams) {
        this.optionNames = optionNames;
        this.optionStates = optionStates;
        this.params = new String[numParams];
    }

    final void parse(Iterator<String> it) throws ExToken {
        int position = 0;
        boolean endOfOptionParsing = false;
        while (it.hasNext()) {
            endOfOptionParsing |= hasOptionParsingEnded(position);
            String token = it.next();
            if (!endOfOptionParsing) {
                if (isEscapeSequence(token)) {
                    endOfOptionParsing = true;
                    continue;
                }
                if (tryReadOption(token, it)) {
                    continue;
                }
                if (SUSPICIOUS.matcher(token).matches()) {
                    throw new ExToken(INVALID_OPTION, token);
                }
            }
            if (position < params.length) {
                params[position++] = token;
            } else {
                handleExcessParam(token);
            }
        }
    }

    abstract boolean hasOptionParsingEnded(int position);

    abstract boolean isEscapeSequence(String token);

    abstract void handleExcessParam(String token) throws ExToken;

    /**
     * Returns the remaining tokens after the last positional parameter.
     *
     * @return remaining tokens
     */
    public abstract List<String> rest();

    /**
     * Parse the given input and store the result internally.
     *
     * <p>The parser is single-use: The parse method should not
     * be called more than once per parser instance.
     *
     * @param tokens command line input
     * @throws ExToken if the input is not valid command line syntax
     */
    public final void parse(List<String> tokens) throws ExToken {
        parse(tokens.iterator());
    }

    private boolean tryReadOption(String token, Iterator<String> it) throws ExToken {
        String name = readOptionName(token);
        if (name == null) {
            return false;
        }
        int opt = optionNames.getOrDefault(name, -1);
        if (opt == -1) {
            return false;
        }
        String t = token;
        while ((t = optionStates[opt].read(t, it)) != null) {
            if ((name = readOptionName(t)) == null) {
                throw new ExToken(ErrTokenType.INVALID_UNIX_GROUP, token);
            }
            if ((opt = optionNames.getOrDefault(name, -1)) == -1) {
                throw new ExToken(ErrTokenType.INVALID_UNIX_GROUP, token);
            }
        }
        return true;
    }

    private String readOptionName(String token) {
        if (token.length() < 2 || !token.startsWith("-")) {
            return null;
        }
        if (!token.startsWith("--")) {
            return token.substring(0, 2);
        }
        if (!token.contains("=")) {
            return token;
        }
        return token.substring(0, token.indexOf('='));
    }

    /**
     * Get all the option values that were passed on the command line
     * for the given option index.
     * The order in which they were passed on the command line is
     * preserved.
     *
     * @param optionIndex a number that identifies a named option
     * @return list of tokens
     */
    public final List<String> option(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= optionStates.length) {
            return List.of();
        }
        OptionState optionState = optionStates[optionIndex];
        return optionState.stream();
    }

    /**
     * Get the positional parameter that was passed on the command line
     * at the given parameter index.
     *
     * <p>Please note: If there are any options before the parameters,
     * the parameter index will be less than the token index.
     *
     * <p>If the index is negative, or if there is no positional parameter at this index,
     * an empty {@code Optional} is returned.
     *
     * @param index parameter index
     * @return the positional parameter at the given index, if it was passed on the command line
     */
    public final Optional<String> param(int index) {
        if (index < 0 || index >= params.length) {
            return Optional.empty();
        }
        return Optional.ofNullable(params[index]);
    }

    final int numParams() {
        return params.length;
    }
}
