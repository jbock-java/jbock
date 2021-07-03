package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapped;
import net.jbock.parameter.PositionalParameter;
import net.jbock.parameter.SourceMethod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;

public class IntermediateResult {

    private final List<SourceMethod> namedOptions;
    private final List<Mapped<PositionalParameter>> positionalParameters;

    private IntermediateResult(
            List<SourceMethod> namedOptions,
            List<Mapped<PositionalParameter>> positionalParameters) {
        this.namedOptions = namedOptions;
        this.positionalParameters = positionalParameters;
    }

    public static Either<List<ValidationFailure>, IntermediateResult> create(
            List<SourceMethod> namedOptions,
            List<Mapped<PositionalParameter>> positionalParameters) {
        List<ValidationFailure> failures = validatePositions(positionalParameters);
        if (!failures.isEmpty()) {
            return left(failures);
        }
        return right(new IntermediateResult(namedOptions, positionalParameters));
    }

    private static List<ValidationFailure> validatePositions(
            List<Mapped<PositionalParameter>> params) {
        List<Mapped<PositionalParameter>> sorted = params.stream()
                .sorted(Comparator.comparing(c -> c.item().position()))
                .collect(Collectors.toUnmodifiableList());
        List<ValidationFailure> failures = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Mapped<PositionalParameter> c = sorted.get(i);
            PositionalParameter p = c.item();
            if (p.position() != i) {
                String message = "Position " + p.position() + " is not available. Suggested position: " + i;
                failures.add(p.fail(message));
            }
        }
        return failures;
    }

    public List<SourceMethod> options() {
        return namedOptions;
    }

    public List<Mapped<PositionalParameter>> positionalParameters() {
        return positionalParameters;
    }
}
