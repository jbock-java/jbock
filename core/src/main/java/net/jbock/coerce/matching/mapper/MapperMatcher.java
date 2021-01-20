package net.jbock.coerce.matching.mapper;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import net.jbock.Mapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.coerce.reference.FunctionType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.ValidationException;
import net.jbock.either.Either;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
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
  private final ReferenceTool referenceTool;

  @Inject
  MapperMatcher(
      ParameterContext context,
      TypeElement mapperClass,
      ImmutableList<Matcher> matchers,
      ReferenceTool referenceTool) {
    super(context);
    this.mapperClass = mapperClass;
    this.matchers = matchers;
    this.referenceTool = referenceTool;
  }

  private Either<String, MapperSuccess> tryAllMatchers() {
    return referenceTool.getReferencedType().chooseRight(functionType -> {
      List<UnwrapSuccess> unwraps = new ArrayList<>();
      for (Matcher matcher : matchers) {
        Either<String, UnwrapSuccess> unwrap = matcher.tryUnwrapReturnType();
        unwrap.ifPresent(unwraps::add);
        Either<String, MapperSuccess> matched = unwrap
            .chooseRight(wrap -> validateMapper(matcher, wrap, functionType));
        if (matched.isPresent()) {
          return matched;
        }
      }
      Optional<String> message = unwraps.stream()
          .max(Comparator.comparingInt(UnwrapSuccess::rank))
          .map(UnwrapSuccess::wrappedType)
          .map(wrappedType -> "No match. Try returning " + wrappedType + " from the mapper");
      return Either.<MapperSuccess, String>fromOptional(null, message).swap();
    });
  }

  public Coercion findCoercion() {
    return commonChecks(mapperClass)
        .chooseRight(this::checkNotAbstract)
        .chooseRight(this::checkNoTypevars)
        .chooseRight(this::checkMapperAnnotation)
        .chooseRight(this::tryAllMatchers)
        .map(success -> new Coercion(enumName(),
            success.mapExpr(),
            success.tailExpr(),
            success.extractExpr(),
            success.skew(),
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

  final Either<String, MapperSuccess> validateMapper(
      Matcher matcher,
      UnwrapSuccess unwrapSuccess,
      FunctionType functionType) {
    return getMapExpr(unwrapSuccess.wrappedType(), functionType)
        .map(mapExpr -> new MapperSuccess(mapExpr, unwrapSuccess, matcher));
  }

  private Either<String, CodeBlock> getMapExpr(
      TypeMirror expectedReturnType,
      FunctionType functionType) {
    if (!tool().isSameType(functionType.inputType(), String.class.getCanonicalName())) {
      return left("The function must accept an input of type String");
    }
    if (!tool().isSameType(functionType.outputType(), expectedReturnType)) {
      return left("The function must return " + expectedReturnType);
    }
    return right(CodeBlock.of("new $T()$L",
        tool().types().erasure(mapperClass.asType()),
        functionType.isSupplier() ? ".get()" : ""));
  }

  private ValidationException mapperFailure(String message) {
    return ValidationException.create(sourceMethod(), String.format("There is a problem with the mapper class: %s.", message));
  }
}
