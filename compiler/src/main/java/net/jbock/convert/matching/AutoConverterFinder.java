package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.convert.Mapped;
import net.jbock.convert.ParameterScope;
import net.jbock.convert.matcher.Matcher;
import net.jbock.either.Either;
import net.jbock.either.Optional;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.SourceMethod;
import net.jbock.validate.ParameterStyle;

import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.jbock.common.Constants.STRING;
import static net.jbock.either.Either.left;

@ParameterScope
public class AutoConverterFinder extends MatchValidator {

  private final AutoConverters autoConverter;
  private final List<Matcher> matchers;
  private final SourceMethod sourceMethod;
  private final Util util;

  @Inject
  AutoConverterFinder(
      AutoConverters autoConverter,
      List<Matcher> matchers,
      SourceMethod sourceMethod,
      ParameterStyle parameterStyle,
      Util util) {
    super(parameterStyle);
    this.autoConverter = autoConverter;
    this.matchers = matchers;
    this.sourceMethod = sourceMethod;
    this.util = util;
  }

  public <P extends AbstractItem> Either<String, Mapped<P>> findConverter(P parameter) {
    for (Matcher matcher : matchers) {
      java.util.Optional<Match> match = matcher.tryMatch(parameter);
      if (match.isPresent()) {
        Match m = match.get();
        return validateMatch(m).flatMap(() ->
            findConverter(m, parameter));
      }
    }
    return left(noMatchError(sourceMethod.returnType()));
  }

  private <P extends AbstractItem> Either<String, Mapped<P>> findConverter(Match match, P parameter) {
    return autoConverter.findAutoConverter(match.baseType())
        .flatMapLeft(this::enumConverter)
        .map(mapExpr -> match.toConvertedParameter(mapExpr, parameter));
  }

  private Either<String, MapExpr> enumConverter(TypeMirror baseType) {
    return asEnumType(baseType)
        .orElseLeft(() -> noMatchError(baseType))
        .map(TypeElement::asType)
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

  private Optional<TypeElement> asEnumType(TypeMirror type) {
    return TypeTool.AS_DECLARED.visit(type)
        .map(DeclaredType::asElement)
        .flatMap(TypeTool.AS_TYPE_ELEMENT::visit)
        .filter(element -> element.getKind() == ElementKind.ENUM)
        .map(Optional::of)
        .orElse(Optional.empty());
  }

  private String noMatchError(TypeMirror type) {
    return "define a converter that implements Function<String, " + util.typeToString(type) + ">";
  }
}
