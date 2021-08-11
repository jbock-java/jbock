package net.jbock.convert.map;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.common.ValidationFailure;
import net.jbock.convert.Mapping;
import net.jbock.convert.match.Match;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.jbock.util.Either.left;
import static net.jbock.common.Constants.STRING;

@ValidateScope
public class AutoOrEnumMapper {

    private final AutoMappings autoMapper;
    private final Util util;

    @Inject
    AutoOrEnumMapper(
            AutoMappings autoMapper,
            Util util) {
        this.autoMapper = autoMapper;
        this.util = util;
    }

    public <M extends AnnotatedMethod>
    Either<ValidationFailure, Mapping<M>> findMapping(
            Match<M> match) {
        return autoMapper.findAutoMapping(match)
                .or(() -> findEnumMapping(match))
                .<Either<ValidationFailure, Mapping<M>>>map(Either::right)
                .orElseGet(() -> left(noConverterError(match)));
    }

    private <M extends AnnotatedMethod>
    Optional<Mapping<M>> findEnumMapping(
            Match<M> match) {
        return TypeTool.AS_DECLARED.visit(match.baseType())
                .map(DeclaredType::asElement)
                .flatMap(TypeTool.AS_TYPE_ELEMENT::visit)
                .filter(element -> element.getKind() == ElementKind.ENUM)
                .map(enumType -> {
                    MappingBlock mapping = enumConversionCode(enumType);
                    return Mapping.create(mapping, match);
                });
    }

    private MappingBlock enumConversionCode(TypeElement baseType) {
        ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
        ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e").build();
        ParameterSpec values = ParameterSpec.builder(STRING, "values").build();
        ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
        CodeBlock.Builder code = CodeBlock.builder();
        TypeMirror type = baseType.asType();
        code.add("try {\n").indent()
                .addStatement("return $T.valueOf($N)", type, token)
                .unindent()
                .add("} catch ($T $N) {\n", IllegalArgumentException.class, e).indent()
                .add("$T $N = $T.stream($T.values())\n", STRING, values, Arrays.class, type).indent()
                .add(".map($T::name)\n", type)
                .addStatement(".collect($T.joining($S, $S, $S))", Collectors.class, ", ", "[", "]")
                .unindent()
                .addStatement("$T $N = $N.getMessage() + $S + $N", STRING, message, e, " ", values)
                .addStatement("throw new $T($N)", IllegalArgumentException.class, message)
                .unindent()
                .add("}\n");
        return new MappingBlock(code.build(), true);
    }

    private ValidationFailure noConverterError(Match<?> match) {
        String expectedType = StringConverter.class.getSimpleName() +
                "<" + util.typeToString(match.baseType()) + ">";
        return match.fail("define a converter class that extends " + expectedType +
                " or implements " +
                Supplier.class.getSimpleName() +
                "<" + expectedType + ">");
    }
}
