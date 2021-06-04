package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.ParameterScope;
import net.jbock.convert.matcher.Matcher;
import net.jbock.either.Either;
import net.jbock.parameter.AbstractParameter;
import net.jbock.parameter.SourceMethod;
import net.jbock.util.StringConverter;
import net.jbock.validate.ParameterStyle;

import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

  public <P extends AbstractParameter> Either<String, ConvertedParameter<P>> findConverter(P parameter) {
    for (Matcher matcher : matchers) {
      Optional<Match> match = matcher.tryMatch(parameter);
      if (match.isPresent()) {
        Match m = match.get();
        return Either.unbalancedLeft(validateMatch(m))
            .flatMap(() -> findConverter(m, parameter));
      }
    }
    return left(noMatchError(sourceMethod.returnType()));
  }

  private <P extends AbstractParameter> Either<String, ConvertedParameter<P>> findConverter(Match match, P parameter) {
    return autoConverter.findAutoConverter(match.baseType())
        .flatMapLeft(this::enumConverter)
        .map(mapExpr -> match.toConvertedParameter(mapExpr, parameter));
  }

  private Either<String, CodeBlock> enumConverter(TypeMirror baseType) {
    if (!isEnumType(baseType)) {
      return left(noMatchError(baseType));
    }
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e").build();
    ParameterSpec values = ParameterSpec.builder(STRING, "values").build();
    ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
    return Either.right(CodeBlock.builder()
        .add(".map($T.create(", StringConverter.class)
        .add("$N -> {\n", s).indent()
        .add("try {\n").indent()
        .add("return $T.valueOf($N);\n", baseType, s)
        .unindent()
        .add("} catch ($T $N) {\n", IllegalArgumentException.class, e).indent()
        .add("$T $N = $T.stream($T.values())\n", STRING, values, Arrays.class, baseType).indent()
        .add(".map($T::name)\n", baseType)
        .add(".collect($T.joining($S, $S, $S));\n", Collectors.class, ", ", "[", "]")
        .unindent()
        .add("$T $N = $N.getMessage() + $S + $N;\n", STRING, message, e, " ", values)
        .add("throw new $T($N);\n", IllegalArgumentException.class, message)
        .unindent().add("}\n")
        .unindent().add("}))\n").build());
  }

  private boolean isEnumType(TypeMirror type) {
    return TypeTool.AS_DECLARED.visit(type)
        .map(DeclaredType::asElement)
        .flatMap(TypeTool.AS_TYPE_ELEMENT::visit)
        .map(element -> element.getKind() == ElementKind.ENUM)
        .orElse(false);
  }

  private String noMatchError(TypeMirror type) {
    return "define a converter that implements Function<String, " + util.typeToString(type) + ">";
  }
}
