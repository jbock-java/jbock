package net.jbock.state;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class GenericParser<T> {

    private final Pattern sus = Pattern.compile("-[a-zA-Z0-9]+|--[a-zA-Z0-9-]+");
    private final Map<String, T> optionNames;
    private final Map<T, OptionState> optionStates;
    private final String[] params;

    GenericParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionStates,
            int numParams) {
        this.optionNames = optionNames;
        this.optionStates = optionStates;
        this.params = new String[numParams];
    }

    abstract GenericParser<T> parse(Iterator<String> it) throws ExToken;

    public final GenericParser<T> parse(List<String> tokens) throws ExToken {
        return parse(tokens.iterator());
    }

    boolean tryReadOption(String token, Iterator<String> it) throws ExToken {
        String name = readOptionName(token);
        if (name == null) {
            return false;
        }
        T opt = optionNames.get(name);
        if (opt == null) {
            return false;
        }
        String t = token;
        while ((t = optionStates.get(opt).read(t, it)) != null) {
            name = readOptionName(t);
            if (name == null) {
                throw new ExToken(ErrTokenType.INVALID_UNIX_GROUP, token);
            }
            if ((opt = optionNames.get(name)) == null) {
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

    public abstract Stream<String> rest();

    public final Stream<String> option(T option) {
        return optionStates.get(option).stream();
    }

    public final Optional<String> param(int index) {
        return Optional.ofNullable(params[index]);
    }

    final void setParam(int index, String token) {
        this.params[index] = token;
    }

    final int numParams() {
        return params.length;
    }

    final boolean suspicious(String token) {
        return sus.matcher(token).matches();
    }
}