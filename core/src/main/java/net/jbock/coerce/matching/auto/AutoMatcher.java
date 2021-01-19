package net.jbock.coerce.matching.auto;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import net.jbock.coerce.AutoMapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;
import java.util.AbstractMap;
import java.util.Map;

import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

public class AutoMatcher extends ParameterScoped {

  private static final String ENUM = Enum.class.getCanonicalName();

  private final AutoMapper autoMapper;
  private final ImmutableList<Matcher> matchers;

  @Inject
  AutoMatcher(
      ParameterContext context,
      AutoMapper autoMapper,
      ImmutableList<Matcher> matchers) {
    super(context);
    this.autoMapper = autoMapper;
    this.matchers = matchers;
  }

  public Coercion findCoercion() {
    return tryFindCoercion()
        .flatMap(entry -> {
          Matcher matcher = entry.getKey();
          UnwrapSuccess unwrapSuccess = entry.getValue();
          return findMapExpr(unwrapSuccess.wrappedType()).map(mapExpr ->
              new Coercion(enumName(), mapExpr, matcher.tailExpr(),
                  unwrapSuccess.extractExpr(), matcher.skew().widen(), unwrapSuccess.constructorParam()));
        })
        .orElseThrow(message -> failure(String.format("Unknown parameter type: %s. Try defining a custom mapper or collector.",
            returnType())));
  }

  private Either<String, Map.Entry<Matcher, UnwrapSuccess>> tryFindCoercion() {
    for (Matcher matcher : matchers) {
      Either<String, UnwrapSuccess> success = matcher.tryUnwrapReturnType();
      if (success.isPresent()) {
        return success.map(s -> new AbstractMap.SimpleImmutableEntry<>(matcher, s));
      }
    }
    return left();
  }

  private Either<String, CodeBlock> findMapExpr(TypeMirror unwrappedReturnType) {
    Either<String, CodeBlock> mapExpr = autoMapper.findAutoMapper(unwrappedReturnType);
    if (mapExpr.isPresent()) {
      return mapExpr;
    }
    if (isEnumType(unwrappedReturnType)) {
      return right(CodeBlock.of("$T::valueOf", unwrappedReturnType));
    }
    return left();
  }

  private boolean isEnumType(TypeMirror type) {
    return types().directSupertypes(type).stream()
        .anyMatch(t -> tool().isSameErasure(t, ENUM));
  }
}
