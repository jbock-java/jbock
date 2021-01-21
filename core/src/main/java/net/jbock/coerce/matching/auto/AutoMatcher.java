package net.jbock.coerce.matching.auto;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.coerce.AutoMapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Skew;
import net.jbock.coerce.Util;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.type.TypeMirror;

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

  public Either<String, Coercion> findCoercion() {
    for (Matcher matcher : matchers) {
      Either<String, UnwrapSuccess> success = matcher.tryUnwrapReturnType();
      if (success.isPresent()) {
        return success.chooseRight(unwrapSuccess ->
            findMapExpr(unwrapSuccess.wrappedType()).map(mapExpr -> {
              CodeBlock tailExpr = matcher.tailExpr();
              CodeBlock extractExpr = unwrapSuccess.extractExpr();
              Skew skew = matcher.skew();
              ParameterSpec constructorParam = unwrapSuccess.constructorParam();
              return new Coercion(enumName(), mapExpr, tailExpr, extractExpr, skew, constructorParam);
            }));
      }
    }
    return left(Util.noMatchError(returnType()));
  }

  private Either<String, CodeBlock> findMapExpr(TypeMirror unwrappedReturnType) {
    Either<String, CodeBlock> mapExpr = autoMapper.findAutoMapper(unwrappedReturnType);
    if (mapExpr.isPresent()) {
      return mapExpr;
    }
    if (isEnumType(unwrappedReturnType)) {
      return right(CodeBlock.of("$T::valueOf", unwrappedReturnType));
    }
    return left(Util.noMatchError(unwrappedReturnType));
  }

  private boolean isEnumType(TypeMirror type) {
    return types().directSupertypes(type).stream()
        .anyMatch(t -> tool().isSameErasure(t, ENUM));
  }
}
