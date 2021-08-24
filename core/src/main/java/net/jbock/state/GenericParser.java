package net.jbock.state;

import net.jbock.util.ErrTokenType;
import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class GenericParser<T> {

    private final Pattern sus = Pattern.compile("-[a-zA-Z0-9]+|--[a-zA-Z0-9-]+");
    private final Map<String, T> optionNames;
    private final Map<T, OptionState> optionParsers;
    private final String[] params;

    GenericParser(
            Map<String, T> optionNames,
            Map<T, OptionState> optionParsers,
            int numParams) {
        this.optionNames = optionNames;
        this.optionParsers = optionParsers;
        this.params = new String[numParams];
    }

    public abstract GenericParser<T> parse(Iterator<String> it) throws ExToken;

    boolean tryReadOption(String token, Iterator<String> it) throws ExToken {
        T opt = optionNames.get(readOptionName(token));
        if (opt == null)
            return false;
        String t = token;
        while ((t = optionParsers.get(opt).read(t, it)) != null) {
            if ((opt = optionNames.get(readOptionName(t))) == null) {
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
        return optionParsers.get(option).stream();
    }

    public final Optional<String> param(int index) {
        return Optional.of(params[index]);
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
