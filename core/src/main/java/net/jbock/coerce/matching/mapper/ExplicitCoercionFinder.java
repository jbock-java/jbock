package net.jbock.coerce.matching.mapper;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.CodeBlock;
import net.jbock.Command;
import net.jbock.Converter;
import net.jbock.coerce.Coercion;
import net.jbock.coerce.Util;
import net.jbock.coerce.matching.Match;
import net.jbock.coerce.matching.matcher.Matcher;
import net.jbock.coerce.reference.FunctionType;
import net.jbock.coerce.reference.ReferenceTool;
import net.jbock.compiler.ParameterContext;
import net.jbock.compiler.ParameterScoped;
import net.jbock.compiler.parameter.AbstractParameter;
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

public class ExplicitCoercionFinder extends ParameterScoped {

  private final ImmutableList<Matcher> matchers;
  private final ReferenceTool referenceTool;

  @Inject
  ExplicitCoercionFinder(
      ParameterContext context,
      ImmutableList<Matcher> matchers,
      ReferenceTool referenceTool) {
    super(context);
    this.matchers = matchers;
    this.referenceTool = referenceTool;
  }

  public <P extends AbstractParameter> Either<String, Coercion<P>> findCoercion(
      P parameter,
      TypeElement converter) {
    Optional<String> maybeFailure = commonChecks(converter).map(s -> "converter " + s);
    return Either.<String, Void>fromFailure(maybeFailure, null)
        .filter(nothing -> checkNotAbstract(converter))
        .filter(nothing -> checkNoTypevars(converter))
        .filter(nothing -> checkMapperAnnotation(converter))
        .flatMap(nothing -> referenceTool.getReferencedType(converter))
        .flatMap(functionType -> tryAllMatchers(functionType, parameter, converter));
  }

  private <P extends AbstractParameter> Either<String, Coercion<P>> tryAllMatchers(
      FunctionType functionType,
      P parameter,
      TypeElement converter) {
    List<Match> matches = new ArrayList<>();
    for (Matcher matcher : matchers) {
      Optional<Match> match = matcher.tryMatch(parameter);
      match.ifPresent(matches::add);
      match = match.filter(m -> isValidMatch(m, functionType));
      if (match.isPresent()) {
        return Either.fromSuccess("", match)
            .map(m -> {
              CodeBlock mapExpr = getMapExpr(functionType, converter);
              return m.toCoercion(mapExpr, parameter);
            });
      }
    }
    Match message = matches.stream()
        .max(Comparator.comparing(Match::skew))
        .orElseThrow(AssertionError::new); // exact matcher always matches
    return Either.left(ExplicitCoercionFinder.noMatchError(message.baseReturnType()));
  }

  private Optional<String> checkMapperAnnotation(TypeElement converter) {
    Converter converterAnnotation = converter.getAnnotation(Converter.class);
    boolean nestedMapper = getEnclosingElements(converter).contains(sourceElement());
    if (converterAnnotation == null && !nestedMapper) {
      return Optional.of("converter must be an inner class of the command class, or carry the @" + Converter.class.getSimpleName() + " annotation");
    }
    return Optional.empty();
  }

  private Optional<String> checkNotAbstract(TypeElement converter) {
    if (converter.getModifiers().contains(ABSTRACT)) {
      return Optional.of("non-abstract converter class");
    }
    return Optional.empty();
  }

  private Optional<String> checkNoTypevars(TypeElement converter) {
    if (!converter.getTypeParameters().isEmpty()) {
      return Optional.of("found type parameters in converter class declaration");
    }
    return Optional.empty();
  }

  private CodeBlock getMapExpr(FunctionType functionType, TypeElement converter) {
    return CodeBlock.of("new $T()$L", converter.asType(),
        functionType.isSupplier() ? ".get()" : "");
  }

  private boolean isValidMatch(Match match, FunctionType functionType) {
    return tool().isSameType(functionType.outputType(), match.baseReturnType());
  }

  private static String noMatchError(TypeMirror type) {
    return "converter should implement Function<String, " + Util.typeToString(type) + ">";
  }
}
