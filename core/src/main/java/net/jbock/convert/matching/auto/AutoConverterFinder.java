package net.jbock.convert.matching.auto;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import dagger.Reusable;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.convert.AutoConverter;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Util;
import net.jbock.convert.matching.ConverterValidator;
import net.jbock.convert.matching.Match;
import net.jbock.convert.matching.matcher.Matcher;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.STRING;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

@Reusable
public class AutoConverterFinder extends ConverterValidator {

  private static final String ENUM = Enum.class.getCanonicalName();

  private final AutoConverter autoConverter;
  private final List<Matcher> matchers;
  private final SourceMethod sourceMethod;
  private final Types types;
  private final TypeTool tool;

  @Inject
  AutoConverterFinder(
      AutoConverter autoConverter,
      List<Matcher> matchers,
      SourceMethod sourceMethod,
      Types types,
      TypeTool tool,
      ParameterStyle parameterStyle) {
    super(parameterStyle);
    this.autoConverter = autoConverter;
    this.matchers = matchers;
    this.sourceMethod = sourceMethod;
    this.types = types;
    this.tool = tool;
  }

  public <P extends AbstractParameter> Either<String, ConvertedParameter<P>> findConverter(P parameter) {
    for (Matcher matcher : matchers) {
      Optional<Match> match = matcher.tryMatch(parameter);
      if (match.isPresent()) {
        Match m = match.get();
        return Either.ofLeft(validateMatch(m)).orRight(null)
            .flatMap(nothing -> findConverter(m, parameter));
      }
    }
    return left(noMatchError(sourceMethod.returnType()));
  }

  private <P extends AbstractParameter> Either<String, ConvertedParameter<P>> findConverter(Match match, P parameter) {
    TypeMirror baseType = match.baseType();
    return autoConverter.findAutoConverter(baseType)
        .flatMapLeft(__ -> isEnumType(baseType) ?
            right(enumConverter(baseType)) :
            left(null))
        .mapLeft(__ -> noMatchError(baseType))
        .map(mapExpr -> match.toCoercion(mapExpr, parameter));
  }

  private CodeBlock enumConverter(TypeMirror baseReturnType) {
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e").build();
    ParameterSpec values = ParameterSpec.builder(STRING, "values").build();
    ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
    return CodeBlock.builder()
        .add(".map(")
        .add("$N -> {\n", s).indent()
        .add("try {\n").indent()
        .add("return $T.valueOf($N);\n", baseReturnType, s)
        .unindent()
        .add("} catch ($T $N) {\n", IllegalArgumentException.class, e).indent()
        .add("$T $N = $T.stream($T.values())\n", STRING, values, Arrays.class, baseReturnType).indent()
        .add(".map($T::name)\n", baseReturnType)
        .add(".collect($T.joining($S, $S, $S));\n", Collectors.class, ", ", "[", "]")
        .unindent()
        .add("$T $N = $N.getMessage() + $S + $N;\n", STRING, message, e, " ", values)
        .add("throw new $T($N);\n", IllegalArgumentException.class, message)
        .unindent().add("}\n")
        .unindent().add("})\n").build();
  }

  private boolean isEnumType(TypeMirror type) {
    return types.directSupertypes(type).stream()
        .anyMatch(t -> tool.isSameErasure(t, ENUM));
  }

  private static String noMatchError(TypeMirror type) {
    return "define a converter that implements Function<String, " + Util.typeToString(type) + ">";
  }
}
