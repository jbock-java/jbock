package net.jbock.coerce.matching;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import net.jbock.Mapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.NonFlagCoercion;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.ValidationException;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.SuppliedClassValidator.getEnclosingElements;
import static net.jbock.coerce.either.Either.left;

public class MapperMatcher extends ParameterScoped {

  private final ImmutableList<Matcher> matchers;

  @Inject
  MapperMatcher(ParameterContext context, OptionalMatcher optionalMatcher, ListMatcher listMatcher, ExactMatcher exactMatcher) {
    super(context);
    this.matchers = ImmutableList.of(optionalMatcher, listMatcher, exactMatcher);
  }

  private MatchingSuccess findCoercion(TypeElement mapperClass) {
    commonChecks(mapperClass);
    checkNotAbstract(mapperClass);
    checkMapperAnnotation(mapperClass);
    Either<String, MatchingSuccess> either = left("");
    try {
      for (Matcher matcher : matchers) {
        either = matcher.tryMatch(mapperClass);
        if (either instanceof Right) {
          return ((Right<String, MatchingSuccess>) either).value();
        }
      }
    } catch (ValidationException e) {
      throw boom(e.getMessage());
    }
    return either.orElseThrow(this::boom);
  }

  public Coercion findMyCoercion(TypeElement mapperClass) {
    MatchingSuccess success = findCoercion(mapperClass);
    CodeBlock expr = MatchingAttempt.autoCollectExpr(optionType(), enumName(), success.skew);
    return new NonFlagCoercion(enumName(), success.mapExpr, expr, success.extractExpr, success.skew, success.constructorParam);
  }

  private void checkMapperAnnotation(TypeElement mapperClass) {
    Mapper mapperAnnotation = mapperClass.getAnnotation(Mapper.class);
    boolean nestedMapper = getEnclosingElements(mapperClass).contains(sourceElement());
    if (mapperAnnotation == null && !nestedMapper) {
      throw boom("The class must either be an inner class of " + sourceElement() +
          ", or carry the " + Mapper.class.getCanonicalName() + " annotation");
    }
  }

  private static void checkNotAbstract(TypeElement typeElement) {
    if (typeElement.getModifiers().contains(ABSTRACT)) {
      throw ValidationException.create(typeElement, "The class may not be abstract.");
    }
  }

  private ValidationException boom(String message) {
    return failure(enrichMessage(message));
  }

  private String enrichMessage(String message) {
    return String.format("There is a problem with the mapper class: %s.", message);
  }
}
