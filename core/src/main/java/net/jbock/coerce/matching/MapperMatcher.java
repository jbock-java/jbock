package net.jbock.coerce.matching;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterSpec;
import net.jbock.Mapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.NonFlagSkew;
import net.jbock.coerce.either.Either;
import net.jbock.coerce.either.Right;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.ValidationException;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.coerce.NonFlagSkew.OPTIONAL;
import static net.jbock.coerce.NonFlagSkew.REPEATABLE;
import static net.jbock.coerce.NonFlagSkew.REQUIRED;
import static net.jbock.coerce.SuppliedClassValidator.commonChecks;
import static net.jbock.coerce.SuppliedClassValidator.getEnclosingElements;
import static net.jbock.coerce.either.Either.left;

public class MapperMatcher extends ParameterScoped {

  @Inject
  MapperMatcher(ParameterContext context) {
    super(context);
  }

  private List<MatchingAttempt> getAttempts(TypeElement mapperClass) {
    TypeMirror returnType = returnType();
    Optional<Optionalish> opt = Optionalish.unwrap(returnType, tool());
    Optional<TypeMirror> listWrapped = tool().getSingleTypeArgument(returnType, List.class.getCanonicalName());
    List<MatchingAttempt> attempts = new ArrayList<>();
    opt.ifPresent(optional -> {
      ParameterSpec param = constructorParam(optional.liftedType());
      // optional match
      attempts.add(attempt(optional.wrappedType(), optional.extractExpr(param), param, OPTIONAL, mapperClass));
      // exact match (-> required)
      attempts.add(attempt(optional.liftedType(), optional.extractExpr(param), param, REQUIRED, mapperClass));
    });
    listWrapped.ifPresent(wrapped -> {
      ParameterSpec param = constructorParam(returnType);
      // list match
      attempts.add(attempt(wrapped, param, REPEATABLE, mapperClass));
    });
    ParameterSpec param = constructorParam(returnType);
    // exact match (-> required)
    attempts.add(attempt(boxedReturnType(), param, REQUIRED, mapperClass));
    return attempts;
  }

  public Coercion findCoercion(TypeElement mapperClass) {
    commonChecks(mapperClass);
    checkNotAbstract(mapperClass);
    checkMapperAnnotation(mapperClass);
    try {
      List<MatchingAttempt> attempts = getAttempts(mapperClass);
      Either<String, Coercion> either = left("");
      for (MatchingAttempt attempt : attempts) {
        either = attempt.findCoercion(this);
        if (either instanceof Right) {
          return ((Right<String, Coercion>) either).value();
        }
      }
      return either.orElseThrow(this::boom);
    } catch (ValidationException e) {
      throw boom(e.getMessage());
    }
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

  private MatchingAttempt attempt(TypeMirror expectedReturnType, CodeBlock extractExpr, ParameterSpec constructorParam, NonFlagSkew skew, TypeElement mapperClass) {
    return new MatchingAttempt(expectedReturnType, extractExpr, constructorParam, skew, mapperClass);
  }

  private MatchingAttempt attempt(TypeMirror expectedReturnType, ParameterSpec constructorParam, NonFlagSkew skew, TypeElement mapperClass) {
    return new MatchingAttempt(expectedReturnType, CodeBlock.of("$N", constructorParam), constructorParam, skew, mapperClass);
  }

  private ValidationException boom(String message) {
    return failure(enrichMessage(message));
  }

  private String enrichMessage(String message) {
    return String.format("There is a problem with the mapper class: %s.", message);
  }
}
