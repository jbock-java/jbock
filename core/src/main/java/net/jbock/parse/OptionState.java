package net.jbock.parse;

import net.jbock.util.ExToken;

import java.util.Iterator;
import java.util.stream.Stream;

public interface OptionState {

    String read(String token, Iterator<String> it) throws ExToken;

    Stream<String> stream();
}
