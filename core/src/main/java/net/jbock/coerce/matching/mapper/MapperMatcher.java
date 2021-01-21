package net.jbock.coerce.matching.mapper;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import net.jbock.Command;
import net.jbock.Mapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Util;
import net.jbock.coerce.matching.UnwrapSuccess;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.coerce.reference.FunctionType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
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

  public Either<String, Coercion> findCoercion() {
    return commonChecks(mapperClass).mapLeft(s -> "mapper " + s)
        .chooseRight(this::checkNotAbstract)
        .chooseRight(this::checkNoTypevars)
        .chooseRight(this::checkMapperAnnotation)
        .chooseRight(referenceTool::getReferencedType)
        .chooseRight(this::checkStringInput)
        .chooseRight(this::tryAllMatchers)
        .map(success -> new Coercion(enumName(),
            success.mapExpr(),
            success.tailExpr(),
            success.extractExpr(),
            success.skew(),
            success.constructorParam()));
  }

  private Either<String, MapperSuccess> tryAllMatchers(FunctionType functionType) {
    List<UnwrapSuccess> unwraps = new ArrayList<>();
    for (Matcher matcher : matchers) {
      Either<String, UnwrapSuccess> unwrap = matcher.tryUnwrapReturnType();
      unwrap.ifPresent(unwraps::add);
      Either<String, MapperSuccess> success = unwrap
          .chooseRight(wrap -> getMapExpr(wrap.wrappedType(), functionType)
              .map(mapExpr -> new MapperSuccess(mapExpr, wrap, matcher)));
      if (success.isPresent()) {
        return success;
      }
    }
    Optional<String> message = unwraps.stream()
        .max(Comparator.comparingInt(UnwrapSuccess::rank))
        .map(UnwrapSuccess::wrappedType)
        .map(Util::noMatchError);
    return Either.<MapperSuccess, String>fromOptional(null, message).swap();
  }

  private Either<String, Void> checkMapperAnnotation() {
    Mapper mapperAnnotation = mapperClass.getAnnotation(Mapper.class);
    boolean nestedMapper = getEnclosingElements(mapperClass).contains(sourceElement());
    if (mapperAnnotation == null && !nestedMapper) {
      return left("mapper must be a static inner class of the @" + Command.class.getSimpleName() +
          " annotated class, or carry the @" + Mapper.class.getSimpleName() + " annotation");
    }
    return right();
  }

  private Either<String, Void> checkNotAbstract() {
    if (mapperClass.getModifiers().contains(ABSTRACT)) {
      return left("non-abstract mapper class");
    }
    return right();
  }

  private Either<String, Void> checkNoTypevars() {
    if (!mapperClass.getTypeParameters().isEmpty()) {
      return left("found type parameters in mapper class declaration");
    }
    return right();
  }

  private Either<String, CodeBlock> getMapExpr(
      TypeMirror expectedReturnType,
      FunctionType functionType) {
    if (!tool().isSameType(functionType.outputType(), expectedReturnType)) {
      return left();
    }
    CodeBlock mapExpr = CodeBlock.of("new $T()$L", mapperClass.asType(),
        functionType.isSupplier() ? ".get()" : "");
    return right(mapExpr);
  }

  private Either<String, FunctionType> checkStringInput(FunctionType functionType) {
    if (!tool().isSameType(functionType.inputType(), String.class.getCanonicalName())) {
      return left("mapper must accept an input of type String");
    }
    return right(functionType);
  }
}
