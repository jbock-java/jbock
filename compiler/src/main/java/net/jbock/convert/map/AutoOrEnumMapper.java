package net.jbock.convert.map;

import io.jbock.javapoet.CodeBlock;
import io.jbock.util.Either;
import jakarta.inject.Inject;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.contrib.EnumConverter;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import java.util.Optional;
import java.util.function.Supplier;

import static io.jbock.util.Either.left;

@ValidateScope
public class AutoOrEnumMapper {

    private final AutoMappings autoMappings;

    @Inject
    AutoOrEnumMapper(
            AutoMappings autoMappings) {
        this.autoMappings = autoMappings;
    }

    public <M extends AnnotatedMethod<?>>
    Either<ValidationFailure, Mapping<M>> findMapping(
            Match<M> match) {
        return autoMappings.findAutoMapping(match)
                .or(() -> findEnumMapping(match))
                .<Either<ValidationFailure, Mapping<M>>>map(Either::right)
                .orElseGet(() -> left(noConverterError(match)));
    }

    private <M extends AnnotatedMethod<?>>
    Optional<Mapping<M>> findEnumMapping(
            Match<M> match) {
        return TypeTool.AS_DECLARED.visit(match.baseType())
                .map(DeclaredType::asElement)
                .flatMap(TypeTool.AS_TYPE_ELEMENT::visit)
                .filter(element -> element.getKind() == ElementKind.ENUM)
                .map(enumType -> {
                    CodeBlock mapper = CodeBlock.of("$1T.create($2T::valueOf, $2T::values)",
                            EnumConverter.class, enumType.asType());
                    return Mapping.create(mapper, match);
                });
    }

    private ValidationFailure noConverterError(Match<?> match) {
        String expectedType = StringConverter.class.getSimpleName() +
                "<" + Util.typeToString(match.baseType()) + ">";
        return match.fail("define a converter class that extends " + expectedType +
                " or implements " +
                Supplier.class.getSimpleName() +
                "<" + expectedType + ">");
    }
}
