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
        return Either.halfLeft(validateMatch(m))
            .flatMap(() -> findConverter(m, parameter));
      }
    }
    return left(noMatchError(sourceMethod.returnType()));
  }

  private <P extends AbstractParameter> Either<String, ConvertedParameter<P>> findConverter(Match match, P parameter) {
    return autoConverter.findAutoConverter(match.baseType())
        .flatMapLeft(this::enumConverter)
        .mapLeft(AutoConverterFinder::noMatchError)
        .map(mapExpr -> match.toConvertedParameter(mapExpr, parameter));
  }

  private Either<TypeMirror, Optional<CodeBlock>> enumConverter(TypeMirror baseType) {
    if (!isEnumType(baseType)) {
      return left(baseType);
    }
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e").build();
    ParameterSpec values = ParameterSpec.builder(STRING, "values").build();
    ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
    return Either.right(Optional.of(CodeBlock.builder()
        .add(".map(")
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
        .unindent().add("})\n").build()));
  }

  private boolean isEnumType(TypeMirror type) {
    return types.directSupertypes(type).stream()
        .anyMatch(t -> tool.isSameErasure(t, ENUM));
  }

  private static String noMatchError(TypeMirror type) {
    return "define a converter that implements Function<String, " + Util.typeToString(type) + ">";
  }
}
