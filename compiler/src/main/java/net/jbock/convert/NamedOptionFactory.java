package net.jbock.convert;

import io.jbock.util.Either;
import net.jbock.common.ValidationFailure;
import net.jbock.parameter.NamedOption;
import net.jbock.source.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static java.lang.Character.isWhitespace;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.convert.Mapped.createFlag;

@ConvertScope
public class NamedOptionFactory {

    // visible for testing
    static final Comparator<String> UNIX_NAMES_FIRST_COMPARATOR = Comparator
            .comparing(String::length)
            .thenComparing(String::toString);

    private final ConverterFinder converterFinder;
    private final Types types;
    private final AnnotationUtil annotationUtil;

    @Inject
    NamedOptionFactory(
            ConverterFinder converterFinder,
            Types types,
            AnnotationUtil annotationUtil) {
        this.converterFinder = converterFinder;
        this.types = types;
        this.annotationUtil = annotationUtil;
    }

    public Either<ValidationFailure, Mapped<NamedOption>> createNamedOption(SourceMethod<?> sourceMethod) {
        Optional<TypeElement> converter = annotationUtil.getConverter(sourceMethod.method());
        return checkOptionNames(sourceMethod)
                .map(names -> new NamedOption(names, sourceMethod))
                .flatMap(namedOption -> {
                    if (converter.isEmpty() && sourceMethod.returnType().getKind() == BOOLEAN) {
                        return right(createFlag(namedOption, types.getPrimitiveType(BOOLEAN)));
                    }
                    return converterFinder.findConverter(namedOption);
                })
                .mapLeft(sourceMethod::fail);
    }

    private Either<String, List<String>> checkOptionNames(SourceMethod<?> sourceMethod) {
        if (sourceMethod.names().isEmpty()) {
            return left("define at least one option name");
        }
        List<String> result = new ArrayList<>();
        for (String name : sourceMethod.names()) {
            Optional<String> check = checkName(name);
            if (check.isPresent()) {
                return left(check.orElseThrow());
            }
            if (result.contains(name)) {
                return left("duplicate option name: " + name);
            }
            result.add(name);
        }
        result.sort(UNIX_NAMES_FIRST_COMPARATOR);
        return right(result);
    }

    /* Left-Optional
     */
    private Optional<String> checkName(String name) {
        if (Objects.toString(name, "").length() <= 1 || "--".equals(name)) {
            return Optional.of("invalid name: " + name);
        }
        if (!name.startsWith("-")) {
            return Optional.of("the name must start with a dash character: " + name);
        }
        if (name.startsWith("---")) {
            return Optional.of("the name must start with one or two dashes, not three:" + name);
        }
        if (!name.startsWith("--") && name.length() > 2) {
            return Optional.of("single-dash names must be single-character names: " + name);
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (isWhitespace(c)) {
                return Optional.of("the name contains whitespace characters: " + name);
            }
            if (c == '=') {
                return Optional.of("the name contains '=': " + name);
            }
        }
        return Optional.empty();
    }
}
