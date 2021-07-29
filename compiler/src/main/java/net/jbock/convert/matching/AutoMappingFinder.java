package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import io.jbock.util.Either;
import net.jbock.annotated.AnnotatedMethod;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.convert.Mapping;
import net.jbock.convert.matcher.Matcher;
import net.jbock.source.SourceMethod;
import net.jbock.util.StringConverter;
import net.jbock.validate.ValidateScope;

import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.jbock.util.Either.left;
import static net.jbock.common.Constants.STRING;

@ValidateScope
public class AutoMappingFinder extends MatchValidator {

    private final AutoMappings autoConverter;
    private final List<Matcher> matchers;
    private final Util util;

    @Inject
    AutoMappingFinder(
            AutoMappings autoConverter,
            List<Matcher> matchers,
            Util util) {
        this.autoConverter = autoConverter;
        this.matchers = matchers;
        this.util = util;
    }

    public <M extends AnnotatedMethod> Either<String, Mapping<M>> findMapping(SourceMethod<M> parameter) {
        for (Matcher matcher : matchers) {
            Optional<Match> match = matcher.tryMatch(parameter);
            if (match.isPresent()) {
                Match m = match.orElseThrow();
                return validateMatch(parameter, m)
                        .<Either<String, Mapping<M>>>map(Either::left)
                        .orElseGet(() -> findMapping(m, parameter));
            }
        }
        return left(noMatchError(parameter.returnType()));
    }

    private <M extends AnnotatedMethod> Either<String, Mapping<M>> findMapping(Match match, SourceMethod<M> parameter) {
        return autoConverter.findAutoMapping(match.baseType())
                .flatMapLeft(this::findEnumMapping)
                .map(mapExpr -> match.toMapping(mapExpr, parameter));
    }

    private Either<String, MapExpr> findEnumMapping(TypeMirror baseType) {
        return TypeTool.AS_DECLARED.visit(baseType)
                .map(DeclaredType::asElement)
                .flatMap(TypeTool.AS_TYPE_ELEMENT::visit)
                .filter(element -> element.getKind() == ElementKind.ENUM)
                .map(TypeElement::asType)
                .<Either<String, TypeMirror>>map(Either::right)
                .orElseGet(() -> left(noMatchError(baseType)))
                .map(enumType -> {
                    CodeBlock code = enumConvertBlock(enumType);
                    return new MapExpr(code, enumType, true);
                });
    }

    private CodeBlock enumConvertBlock(TypeMirror enumType) {
        ParameterSpec token = ParameterSpec.builder(STRING, "token").build();
        ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e").build();
        ParameterSpec values = ParameterSpec.builder(STRING, "values").build();
        ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
        CodeBlock.Builder code = CodeBlock.builder();
        code.add("try {\n").indent()
                .addStatement("return $T.valueOf($N)", enumType, token)
                .unindent()
                .add("} catch ($T $N) {\n", IllegalArgumentException.class, e).indent()
                .add("$T $N = $T.stream($T.values())\n", STRING, values, Arrays.class, enumType).indent()
                .add(".map($T::name)\n", enumType)
                .addStatement(".collect($T.joining($S, $S, $S))", Collectors.class, ", ", "[", "]")
                .unindent()
                .addStatement("$T $N = $N.getMessage() + $S + $N", STRING, message, e, " ", values)
                .addStatement("throw new $T($N)", IllegalArgumentException.class, message)
                .unindent()
                .add("}\n");
        return code.build();
    }

    private String noMatchError(TypeMirror type) {
        return "define a converter that implements " +
                StringConverter.class.getSimpleName() +
                "<" + util.typeToString(type) + ">";
    }
}
