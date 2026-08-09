package net.jbock.parse;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static net.jbock.util.ErrTokenType.INVALID_OPTION;

/**
 * Abstract superclass of several primitive command line parsers.
 * These parsers are mutable and not thread safe.
 */
abstract class SimpleParser implements ParseResult {

    private static final Pattern SUSPICIOUS = Pattern.compile("^-\\S.*");

    private final Map<String, Integer> optionNames;
    private final OptionState[] optionStates;
    private final String[] params;

    SimpleParser(
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

    @Override
    public final Stream<String> option(int optionIndex) {
        if (optionIndex < 0 || optionIndex >= optionStates.length) {
            return Stream.empty();
        }
        OptionState optionState = optionStates[optionIndex];
        return optionState.stream();
    }

    @Override
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
