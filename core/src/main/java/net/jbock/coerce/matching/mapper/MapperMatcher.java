package net.jbock.coerce.matching.mapper;

import com.google.common.collect.ImmutableList;
import net.jbock.Mapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.SuppliedClassValidator.getEnclosingElements;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

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

  private Either<String, MapperSuccess> tryAllMatchers() {
    List<UnwrapSuccess> successes = new ArrayList<>();
    for (Matcher matcher : matchers) {
      Either<String, UnwrapSuccess> success = matcher.tryUnwrapReturnType();
      success.ifPresent(successes::add);
      Either<String, MapperSuccess> matched = success
          .flatMap(unwrapSuccess -> validateMapper(matcher, unwrapSuccess));
      if (matched.isPresent()) {
        return matched;
      }
    }
    Optional<UnwrapSuccess> success = successes.stream().max(Comparator.comparingInt(UnwrapSuccess::rank));
    return Either.left(success.map(UnwrapSuccess::wrappedType)
        .map(wrappedType -> "No match. Try returning " + wrappedType + " from the mapper")
        .orElse("no match"));
  }

  public Coercion findCoercion() {
    return commonChecks(mapperClass)
        .flatMap(this::checkNotAbstract)
        .flatMap(this::checkNoTypevars)
        .flatMap(this::checkMapperAnnotation)
        .flatMap(this::tryAllMatchers)
        .map(success -> new Coercion(enumName(),
            success.mapExpr(),
            success.tailExpr(),
            success.extractExpr(),
            success.skew().widen(),
            success.constructorParam()))
        .orElseThrow(this::mapperFailure);
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
      return left("The class may not be abstract");
    }
    return right();
  }

  private Either<String, Void> checkNoTypevars() {
    if (!mapperClass.getTypeParameters().isEmpty()) {
      return left("The class may not have any type parameters");
    }
    return right();
  }

  final Either<String, MapperSuccess> validateMapper(Matcher matcher, UnwrapSuccess unwrapSuccess) {
    return mapperClassValidator.getMapExpr(unwrapSuccess.wrappedType())
        .map(mapExpr -> new MapperSuccess(mapExpr, unwrapSuccess, matcher));
  }
}
