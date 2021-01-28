package net.jbock.coerce.matching.mapper;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import net.jbock.Command;
import net.jbock.Mapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Util;
import net.jbock.coerce.matching.Match;
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
    Optional<String> maybeFailure = commonChecks(mapperClass).map(s -> "mapper " + s);
    return Either.<String, Void>fromFailure(maybeFailure, null)
        .filter(this::checkNotAbstract)
        .filter(this::checkNoTypevars)
        .filter(this::checkMapperAnnotation)
        .flatMap(v -> referenceTool.getReferencedType())
        .filter(this::checkStringInput)
        .flatMap(this::tryAllMatchers)
        .map(success -> Coercion.create(success, enumName()));
  }

  private Either<String, MapperSuccess> tryAllMatchers(FunctionType functionType) {
    List<Match> unwraps = new ArrayList<>();
    for (Matcher matcher : matchers) {
      Optional<Match> unwrap = matcher.tryMatch();
      unwrap.ifPresent(unwraps::add);
      Optional<MapperSuccess> success = unwrap
          .flatMap(wrap -> getMapExpr(wrap.typeArg(), functionType)
              .map(mapExpr -> new MapperSuccess(mapExpr, wrap, matcher)));
      if (success.isPresent()) {
        return Either.fromSuccess("", success);
      }
    }
    Match message = unwraps.stream()
        .max(Comparator.comparing(Match::skew))
        .orElseThrow(AssertionError::new);
    return Either.left(MapperMatcher.noMatchError(message.typeArg()));
  }

  private Optional<String> checkMapperAnnotation() {
    Mapper mapperAnnotation = mapperClass.getAnnotation(Mapper.class);
    boolean nestedMapper = getEnclosingElements(mapperClass).contains(sourceElement());
    if (mapperAnnotation == null && !nestedMapper) {
      return Optional.of("mapper must be a static inner class of the @" + Command.class.getSimpleName() +
          " annotated class, or carry the @" + Mapper.class.getSimpleName() + " annotation");
    }
    return Optional.empty();
  }

  private Optional<String> checkNotAbstract() {
    if (mapperClass.getModifiers().contains(ABSTRACT)) {
      return Optional.of("non-abstract mapper class");
    }
    return Optional.empty();
  }

  private Optional<String> checkNoTypevars() {
    if (!mapperClass.getTypeParameters().isEmpty()) {
      return Optional.of("found type parameters in mapper class declaration");
    }
    return Optional.empty();
  }

  private Optional<CodeBlock> getMapExpr(
      TypeMirror expectedReturnType,
      FunctionType functionType) {
    if (!tool().isSameType(functionType.outputType(), expectedReturnType)) {
      return Optional.empty();
    }
    CodeBlock mapExpr = CodeBlock.of("new $T()$L", mapperClass.asType(),
        functionType.isSupplier() ? ".get()" : "");
    return Optional.of(mapExpr);
  }

  private Optional<String> checkStringInput(FunctionType functionType) {
    if (!tool().isSameType(functionType.inputType(), String.class.getCanonicalName())) {
      return Optional.of("mapper should implement Function<String, ?>");
    }
    return Optional.empty();
  }

  private static String noMatchError(TypeMirror type) {
    return "mapper should implement Function<String, " + Util.typeToString(type) + ">";
  }
}
