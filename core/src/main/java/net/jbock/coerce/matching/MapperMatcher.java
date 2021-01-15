package net.jbock.coerce.matching;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.Mapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.MapperClassValidator;
import net.jbock.coerce.NonFlagCoercion;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.ValidationException;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import java.util.function.Function;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.SuppliedClassValidator.getEnclosingElements;
import static net.jbock.coerce.either.Either.left;

public class MapperMatcher extends ParameterScoped {

  private final ImmutableList<Matcher> matchers;
  private final TypeElement mapperClass;

  @Inject
  MapperMatcher(
      ParameterContext context,
      TypeElement mapperClass,
      ImmutableList<Matcher> matchers) {
    super(context);
    this.mapperClass = mapperClass;
    this.matchers = matchers;
  }

  private MatchingSuccess findCoercion() {
    commonChecks(mapperClass);
    checkNotAbstract();
    checkMapperAnnotation();
    Either<String, MatchingSuccess> either = left("");
    try {
      for (Matcher matcher : matchers) {
        either = tryMatch(matcher);
        if (either instanceof Right) {
          return ((Right<String, MatchingSuccess>) either).value();
        }
      }
    } catch (ValidationException e) {
      throw boom(e.getMessage());
    }
    return either.orElseThrow(this::boom);
  }

  public Coercion findMyCoercion() {
    MatchingSuccess success = findCoercion();
    return new NonFlagCoercion(enumName(), success.mapExpr, success.autoCollectExpr, success.extractExpr, success.skew, success.constructorParam);
  }

  private void checkMapperAnnotation() {
    Mapper mapperAnnotation = mapperClass.getAnnotation(Mapper.class);
    boolean nestedMapper = getEnclosingElements(mapperClass).contains(sourceElement());
    if (mapperAnnotation == null && !nestedMapper) {
      throw boom("The class must either be an inner class of " + sourceElement() +
          ", or carry the " + Mapper.class.getCanonicalName() + " annotation");
    }
  }

  private void checkNotAbstract() {
    if (mapperClass.getModifiers().contains(ABSTRACT)) {
      throw ValidationException.create(mapperClass, "The class may not be abstract.");
    }
  }

  final Either<String, MatchingSuccess> tryMatch(Matcher matcher) {
    return matcher.tryUnwrapReturnType()
        .map(unwrapSuccess -> match(matcher, unwrapSuccess))
        .orElseGet(() -> Either.left("no match"));
  }

  final Either<String, MatchingSuccess> match(Matcher matcher, UnwrapSuccess unwrapSuccess) {
    MapperClassValidator validator = new MapperClassValidator(this::failure, tool(), unwrapSuccess.wrappedType(), mapperClass);
    ParameterSpec constructorParam = constructorParam(unwrapSuccess.constructorParamType());
    return validator.getMapExpr().map(Function.identity(), mapExpr ->
        new MatchingSuccess(mapExpr, unwrapSuccess.extractExpr(constructorParam), constructorParam,
            matcher.skew(), matcher.autoCollectExpr()));
  }

  private ValidationException boom(String message) {
    return failure(enrichMessage(message));
  }

  private String enrichMessage(String message) {
    return String.format("There is a problem with the mapper class: %s.", message);
  }
}
