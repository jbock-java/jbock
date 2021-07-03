package net.jbock.convert;

import io.jbock.util.Either;
import io.jbock.util.LeftOptional;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static io.jbock.util.Either.left;
import static io.jbock.util.Either.right;
import static java.lang.Character.isWhitespace;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.convert.Mapped.createFlag;

@ParameterScope
public class NamedOptionFactory {

    // visible for testing
    static final Comparator<String> UNIX_NAMES_FIRST_COMPARATOR = Comparator
            .comparing(String::length)
            .thenComparing(String::toString);

    private final ConverterFinder converterFinder;
    private final ConverterClass converterClass;
    private final SourceMethod sourceMethod;
    private final SourceElement sourceElement;
    private final EnumName enumName;
    private final List<Mapped<NamedOption>> alreadyCreated;
    private final Types types;

    @Inject
    NamedOptionFactory(
            ConverterClass converterClass,
            ConverterFinder converterFinder,
            SourceMethod sourceMethod,
            SourceElement sourceElement,
            EnumName enumName,
            List<Mapped<NamedOption>> alreadyCreated,
            Types types) {
        this.converterFinder = converterFinder;
        this.converterClass = converterClass;
        this.sourceMethod = sourceMethod;
        this.sourceElement = sourceElement;
        this.enumName = enumName;
        this.alreadyCreated = alreadyCreated;
        this.types = types;
    }

    public Either<ValidationFailure, Mapped<NamedOption>> createNamedOption() {
        return checkOptionNames()
                .map(this::createNamedOption)
                .flatMap(namedOption -> {
                    if (!converterClass.isPresent() && sourceMethod.returnType().getKind() == BOOLEAN) {
                        return right(createFlag(namedOption, types.getPrimitiveType(BOOLEAN)));
                    }
                    return converterFinder.findConverter(namedOption);
                })
                .mapLeft(sourceMethod::fail);
    }

    private Either<String, List<String>> checkOptionNames() {
        if (sourceMethod.names().isEmpty()) {
            return left("define at least one option name");
        }
        for (Mapped<NamedOption> c : alreadyCreated) {
            for (String name : sourceMethod.names()) {
                for (String previousName : c.item().names()) {
                    if (name.equals(previousName)) {
                        return left("duplicate option name: " + name);
                    }
                }
            }
        }
        List<String> result = new ArrayList<>();
        for (String name : sourceMethod.names()) {
            LeftOptional<String> check = checkName(name);
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

    private LeftOptional<String> checkName(String name) {
        if (Objects.toString(name, "").length() <= 1 || "--".equals(name)) {
            return LeftOptional.of("invalid name: " + name);
        }
        if (!name.startsWith("-")) {
            return LeftOptional.of("the name must start with a dash character: " + name);
        }
        if (name.startsWith("---")) {
            return LeftOptional.of("the name must start with one or two dashes, not three:" + name);
        }
        if (!name.startsWith("--") && name.length() > 2) {
            return LeftOptional.of("single-dash names must be single-character names: " + name);
        }
        if (sourceElement.helpEnabled() && "--help".equals(name)) {
            return LeftOptional.of("'--help' is reserved, set 'helpEnabled=false' to allow it");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (isWhitespace(c)) {
                return LeftOptional.of("the name contains whitespace characters: " + name);
            }
            if (c == '=') {
                return LeftOptional.of("the name contains '=': " + name);
            }
        }
        return LeftOptional.empty();
    }

    private NamedOption createNamedOption(List<String> names) {
        return new NamedOption(enumName, names, sourceMethod);
    }
}
