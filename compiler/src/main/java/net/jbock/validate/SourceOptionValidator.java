package net.jbock.validate;

import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedOption;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.MappingFinder;
import net.jbock.convert.matching.MapExpr;
import net.jbock.convert.matching.Match;
import net.jbock.source.SourceOption;

import javax.inject.Inject;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static io.jbock.util.Eithers.toOptionalList;
import static io.jbock.util.Eithers.toValidListAll;
import static java.lang.Character.isWhitespace;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.model.Multiplicity.OPTIONAL;

@ValidateScope
public class SourceOptionValidator {

    private final MappingFinder converterFinder;
    private final Types types;

    @Inject
    SourceOptionValidator(
            MappingFinder converterFinder,
            Types types) {
        this.converterFinder = converterFinder;
        this.types = types;
    }

    Either<List<ValidationFailure>, ContextBuilder> wrapOptions(
            ContextBuilder.Step3 step) {
        return step.namedOptions().stream()
                .map(this::checkOptionNames)
                .collect(toValidListAll())
                .filter(this::validateUniqueOptionNames)
                .flatMap(sourceOptions -> sourceOptions.stream()
                        .map(this::wrapOption)
                        .collect(toValidListAll()))
                .map(step::accept);
    }

    private Either<ValidationFailure, Mapping<AnnotatedOption>> wrapOption(SourceOption sourceMethod) {
        return checkFlag(sourceMethod)
                .<Either<ValidationFailure, Mapping<AnnotatedOption>>>map(Either::right)
                .orElseGet(() -> converterFinder.findMapping(sourceMethod).mapLeft(sourceMethod::fail));
    }

    /* Right-Optional
     */
    private Optional<Mapping<AnnotatedOption>> checkFlag(SourceOption sourceOption) {
        if (sourceOption.annotatedMethod().converter().isPresent()) {
            return Optional.empty();
        }
        if (sourceOption.returnType().getKind() != BOOLEAN) {
            return Optional.empty();
        }
        PrimitiveType bool = types.getPrimitiveType(BOOLEAN);
        Match<AnnotatedOption> match = Match.create(bool, OPTIONAL, sourceOption);
        return Optional.of(MapExpr.createFlag(match));
    }

    private Either<ValidationFailure, SourceOption> checkOptionNames(SourceOption sourceMethod) {
        if (sourceMethod.annotatedMethod().names().isEmpty()) {
            return left(sourceMethod.fail("define at least one option name"));
        }
        for (String name : sourceMethod.names()) {
            Optional<String> check = checkName(name);
            if (check.isPresent()) {
                return left(sourceMethod.fail(check.map(s -> "invalid name: " + s)
                        .orElseThrow()));
            }
        }
        return right(sourceMethod);
    }

    /* Left-Optional
     */
    private Optional<String> checkName(String name) {
        if (Objects.toString(name, "").length() <= 1 || "--".equals(name)) {
            return Optional.of(name);
        }
        if (!name.startsWith("-")) {
            return Optional.of("must start with a dash character: " + name);
        }
        if (name.startsWith("---")) {
            return Optional.of("cannot start with three dashes: " + name);
        }
        if (!name.startsWith("--") && name.length() > 2) {
            return Optional.of("single-dash name must be single-character: " + name);
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (isWhitespace(c)) {
                return Optional.of("whitespace characters: " + name);
            }
            if (c == '=') {
                return Optional.of("invalid character '=': " + name);
            }
        }
        return Optional.empty();
    }

    /* Left-Optional
     */
    private Optional<List<ValidationFailure>> validateUniqueOptionNames(List<SourceOption> allOptions) {
        Set<String> allNames = new HashSet<>();
        return allOptions.stream()
                .flatMap(item -> item.names().stream()
                        .filter(name -> !allNames.add(name))
                        .map(name -> "duplicate option name: " + name)
                        .map(item::fail))
                .collect(toOptionalList());
    }
}
