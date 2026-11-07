package net.jbock.validate;

import io.jbock.simple.Inject;
import io.jbock.util.Either;
import net.jbock.annotated.Items;
import net.jbock.annotated.Option;
import net.jbock.common.TypeTool;
import net.jbock.common.ValidationFailure;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.allFailures;
import static io.jbock.util.Eithers.toOptionalList;
import static java.lang.Character.isWhitespace;

@Inject
record OptionValidator(TypeTool tool) {

    Either<List<ValidationFailure>, List<Option>> wrapOptions(
            Items items) {
        return items.options().stream()
                .map(this::checkOption)
                .collect(allFailures())
                .filter(this::validateUniqueOptionNames);
    }

    private Either<ValidationFailure, Option> checkOption(
            Option option) {
        if (option.names().isEmpty()) {
            return left(option.fail("define at least one option name"));
        }
        Optional<ValidationFailure> converterError = option.validateConverterExpression();
        if (converterError.isPresent()) {
            return Either.left(converterError.orElseThrow());
        }
        return option.names().stream()
                .map(name -> checkName(option, name))
                .flatMap(Optional::stream)
                .map(s -> s.prepend("invalid name: "))
                .findFirst()
                .<Either<ValidationFailure, Option>>map(Either::left)
                .orElseGet(() -> right(option));
    }

    /* Left-Optional
     */
    private Optional<ValidationFailure> checkName(Option option, String name) {
        if (Objects.toString(name, "").length() <= 1 || "--".equals(name)) {
            return Optional.of(option.fail(name));
        }
        if (!name.startsWith("-")) {
            return Optional.of(option.fail("must start with a dash character: " + name));
        }
        if (name.startsWith("---")) {
            return Optional.of(option.fail("cannot start with three dashes: " + name));
        }
        if (!name.startsWith("--") && name.length() > 2) {
            return Optional.of(option.fail("single-dash name must be single-character: " + name));
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (isWhitespace(c)) {
                return Optional.of(option.fail("whitespace characters: " + name));
            }
            if (c == '=') {
                return Optional.of(option.fail("invalid character '=': " + name));
            }
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateUniqueOptionNames(
            List<Option> allOptions) {
        Set<String> allNames = new HashSet<>();
        return allOptions.stream()
                .flatMap(option -> option.names().stream()
                        .filter(name -> !allNames.add(name))
                        .map(name -> "duplicate option name: " + name)
                        .map(option::fail))
                .collect(toOptionalList());
    }
}
