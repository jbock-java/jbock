package net.jbock.coerce.matching.auto;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import net.jbock.Option;
import net.jbock.coerce.AutoMapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionFactory;
import net.jbock.coerce.Util;
import net.jbock.coerce.matching.Match;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

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

  public Either<String, Coercion> findCoercion() {
    if (sourceMethod().getAnnotation(Option.class) != null &&
        tool().isSameType(boxedReturnType(), Boolean.class.getCanonicalName())) {
      return right(coercionFactory.createFlag());
    }
    for (Matcher matcher : matchers) {
      Optional<Match> match = matcher.tryMatch();
      if (match.isPresent()) {
        return Either.fromSuccess("", match)
            .flatMap(this::findMapper);
      }
    }
    return left(noMatchError(returnType()));
  }

  private Either<String, Coercion> findMapper(Match match) {
    TypeMirror baseReturnType = match.baseReturnType();
    return autoMapper.findAutoMapper(baseReturnType)
        .maybeRecover(() -> isEnumType(baseReturnType) ?
            Optional.of(CodeBlock.of("$T::valueOf", baseReturnType)) :
            Optional.empty())
        .mapLeft(s -> noMatchError(baseReturnType))
        .map(mapExpr -> coercionFactory.create(mapExpr, match));
  }

  private boolean isEnumType(TypeMirror type) {
    return types().directSupertypes(type).stream()
        .anyMatch(t -> tool().isSameErasure(t, ENUM));
  }

  private static String noMatchError(TypeMirror type) {
    return "define a mapper that implements Function<String, " + Util.typeToString(type) + ">";
  }
}
