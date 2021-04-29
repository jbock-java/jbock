package net.jbock.coerce.matching.auto;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.AutoMapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionFactory;
import net.jbock.coerce.Util;
import net.jbock.coerce.matching.Match;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.parameter.Parameter;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.jbock.compiler.Constants.STRING;
import static net.jbock.either.Either.left;

public class AutoMatcher extends ParameterScoped {

  private static final String ENUM = Enum.class.getCanonicalName();

  private final AutoMapper autoMapper;
  private final ImmutableList<Matcher> matchers;
  private final CoercionFactory coercionFactory;

  @Inject
  AutoMatcher(
      ParameterContext context,
      AutoMapper autoMapper,
      ImmutableList<Matcher> matchers,
      CoercionFactory coercionFactory) {
    super(context);
    this.autoMapper = autoMapper;
    this.matchers = matchers;
    this.coercionFactory = coercionFactory;
  }

  public <P extends Parameter> Either<String, Coercion<P>> findCoercion(P parameter) {
    for (Matcher matcher : matchers) {
      Optional<Match> match = matcher.tryMatch();
      if (match.isPresent()) {
        return Either.<String, Match>right(match.get())
            .flatMap(m -> findMapper(m, parameter));
      }
    }
    return left(noMatchError(returnType()));
  }

  private <P extends Parameter> Either<String, Coercion<P>> findMapper(Match match, P parameter) {
    TypeMirror baseReturnType = match.baseReturnType();
    return autoMapper.findAutoMapper(baseReturnType)
        .maybeRecover(() -> isEnumType(baseReturnType) ?
            Optional.of(autoMapperEnum(baseReturnType)) :
            Optional.empty())
        .mapLeft(s -> noMatchError(baseReturnType))
        .map(mapExpr -> coercionFactory.create(mapExpr, match, parameter));
  }

  private CodeBlock autoMapperEnum(TypeMirror baseReturnType) {
    ParameterSpec s = ParameterSpec.builder(STRING, "s").build();
    ParameterSpec e = ParameterSpec.builder(IllegalArgumentException.class, "e").build();
    ParameterSpec values = ParameterSpec.builder(STRING, "values").build();
    ParameterSpec message = ParameterSpec.builder(STRING, "message").build();
    return CodeBlock.builder()
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
        .unindent().add("}").build();
  }

  private boolean isEnumType(TypeMirror type) {
    return types().directSupertypes(type).stream()
        .anyMatch(t -> tool().isSameErasure(t, ENUM));
  }

  private static String noMatchError(TypeMirror type) {
    return "define a mapper that implements Function<String, " + Util.typeToString(type) + ">";
  }
}
