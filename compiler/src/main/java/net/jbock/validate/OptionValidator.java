package net.jbock.validate;

import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.annotated.ExecutableOption;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.MappingFinder;

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
import static javax.lang.model.type.TypeKind.BOOLEAN;

@ValidateScope
class OptionValidator {

    private final MappingFinder mappingFinder;

    @Inject
    OptionValidator(MappingFinder mappingFinder) {
        this.mappingFinder = mappingFinder;
    }

    Either<List<ValidationFailure>, ContextBuilder> wrapOptions(
            ContextBuilder.Step3 step) {
        return step.namedOptions().stream()
                .map(this::checkOptionNames)
                .collect(allFailures())
                .filter(this::validateUniqueOptionNames)
                .flatMap(sourceOptions -> sourceOptions.stream()
                        .map(this::wrapOption)
                        .collect(allFailures()))
                .map(step::accept);
    }

    private Either<ValidationFailure, Mapping<ExecutableOption>> wrapOption(
            ExecutableOption option) {
        return checkNullary(option)
                .map(mappingFinder::findNullaryMapping)
                .orElseGet(() -> mappingFinder.findMapping(option));
    }

    private Optional<ExecutableOption> checkNullary(
            ExecutableOption option) {
        if (option.converter().isPresent()) {
            return Optional.empty();
        }
        if (option.returnType().getKind() != BOOLEAN) {
            return Optional.empty();
        }
        return Optional.of(option);
    }

    private Either<ValidationFailure, ExecutableOption> checkOptionNames(
            ExecutableOption option) {
        if (option.names().isEmpty()) {
            return left(option.fail("define at least one option name"));
        }
        return option.names().stream()
                .map(name -> checkName(option, name))
                .flatMap(Optional::stream)
                .map(s -> s.prepend("invalid name: "))
                .findFirst()
                .<Either<ValidationFailure, ExecutableOption>>map(Either::left)
                .orElseGet(() -> right(option));
    }

    /* Left-Optional
     */
    private Optional<ValidationFailure> checkName(ExecutableOption option, String name) {
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
            List<ExecutableOption> allOptions) {
        Set<String> allNames = new HashSet<>();
        return allOptions.stream()
                .flatMap(option -> option.names().stream()
                        .filter(name -> !allNames.add(name))
                        .map(name -> "duplicate option name: " + name)
                        .map(option::fail))
                .collect(toOptionalList());
    }
}
