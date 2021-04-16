package net.jbock.coerce.matching.mapper;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import net.jbock.Command;
import net.jbock.Mapper;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.CoercionFactory;
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
  private final CoercionFactory coercionFactory;

  @Inject
  MapperMatcher(
      ParameterContext context,
      TypeElement mapperClass,
      ImmutableList<Matcher> matchers,
      ReferenceTool referenceTool,
      CoercionFactory coercionFactory) {
    super(context);
    this.mapperClass = mapperClass;
    this.matchers = matchers;
    this.referenceTool = referenceTool;
    this.coercionFactory = coercionFactory;
  }

  public Either<String, Coercion> findCoercion() {
    Optional<String> maybeFailure = commonChecks(mapperClass).map(s -> "mapper " + s);
    return Either.<String, Void>fromFailure(maybeFailure, null)
        .filter(this::checkNotAbstract)
        .filter(this::checkNoTypevars)
        .filter(this::checkMapperAnnotation)
        .flatMap(nothing -> referenceTool.getReferencedType())
        .flatMap(this::tryAllMatchers);
  }

  private Either<String, Coercion> tryAllMatchers(FunctionType functionType) {
    List<Match> matches = new ArrayList<>();
    for (Matcher matcher : matchers) {
      Optional<Match> match = matcher.tryMatch();
      match.ifPresent(matches::add);
      match = match.filter(m -> isValidMatch(m, functionType));
      if (match.isPresent()) {
        return Either.fromSuccess("", match)
            .map(m -> {
              CodeBlock mapExpr = getMapExpr(functionType);
              return coercionFactory.create(mapExpr, m);
            });
      }
    }
    Match message = matches.stream()
        .max(Comparator.comparing(Match::skew))
        .orElseThrow(AssertionError::new); // exact matcher always matches
    return Either.left(MapperMatcher.noMatchError(message.baseReturnType()));
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

  private CodeBlock getMapExpr(FunctionType functionType) {
    return CodeBlock.of("new $T()$L", mapperClass.asType(),
        functionType.isSupplier() ? ".get()" : "");
  }

  private boolean isValidMatch(Match match, FunctionType functionType) {
    return tool().isSameType(functionType.outputType(), match.baseReturnType());
  }

  private static String noMatchError(TypeMirror type) {
    return "mapper should implement Function<String, " + Util.typeToString(type) + ">";
  }
}
