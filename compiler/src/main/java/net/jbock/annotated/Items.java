package net.jbock.annotated;

import java.util.List;

public record Items(
        List<Option> namedOptions, // rename to options
        List<Parameter> positionalParameters) { // rename to parameters
}
