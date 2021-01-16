package net.jbock.coerce.matching;

import com.google.common.collect.ImmutableList;
import net.jbock.Mapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.NonFlagCoercion;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.SuppliedClassValidator.getEnclosingElements;
import static net.jbock.coerce.either.Either.left;
import static net.jbock.coerce.either.Either.right;

public class MapperMatcher extends ParameterScoped {

  private final ImmutableList<Matcher> matchers;
  private final TypeElement mapperClass;
  private final MapperClassValidator mapperClassValidator;

  @Inject
  MapperMatcher(
      ParameterContext context,
      TypeElement mapperClass,
      ImmutableList<Matcher> matchers,
      MapperClassValidator mapperClassValidator) {
    super(context);
    this.mapperClass = mapperClass;
    this.matchers = matchers;
    this.mapperClassValidator = mapperClassValidator;
  }

  private Either<String, MatchingSuccess> findCoercion() {
    return commonChecks(mapperClass)
        .flatMap(this::checkNotAbstract)
        .flatMap(this::checkMapperAnnotation)
        .flatMap(() -> {
          Either<String, MatchingSuccess> match = left("");
          for (Matcher matcher : matchers) {
            match = tryMatch(matcher);
            if (match instanceof Right) {
              return match;
            }
          }
          return match; // report the final mapper failure
        });
  }

  public Coercion findMyCoercion() {
    MatchingSuccess success = findCoercion().orElseThrow(this::mapperFailure);
    return new NonFlagCoercion(enumName(),
        success.mapExpr(),
        success.autoCollectExpr(),
        success.extractExpr(),
        success.skew(),
        success.constructorParam());
  }

  private Either<String, Void> checkMapperAnnotation() {
    Mapper mapperAnnotation = mapperClass.getAnnotation(Mapper.class);
    boolean nestedMapper = getEnclosingElements(mapperClass).contains(sourceElement());
    if (mapperAnnotation == null && !nestedMapper) {
      return left("The class must either be an inner class of " + sourceElement() +
          ", or carry the " + Mapper.class.getCanonicalName() + " annotation");
    }
    return right();
  }

  private Either<String, Void> checkNotAbstract() {
    if (mapperClass.getModifiers().contains(ABSTRACT)) {
      return left("The class may not be abstract.");
    }
    return right();
  }

  final Either<String, MatchingSuccess> tryMatch(Matcher matcher) {
    return matcher.tryUnwrapReturnType()
        .map(unwrapSuccess -> match(matcher, unwrapSuccess))
        .orElseGet(() -> left("no match"));
  }

  final Either<String, MatchingSuccess> match(Matcher matcher, UnwrapSuccess unwrapSuccess) {
    return mapperClassValidator.getMapExpr(unwrapSuccess.wrappedType())
        .map(mapExpr -> new MatchingSuccess(mapExpr, unwrapSuccess, matcher));
  }
}
