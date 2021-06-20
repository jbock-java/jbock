package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.Converter;
import net.jbock.common.Util;
import net.jbock.convert.Mapped;
import net.jbock.convert.ParameterScope;
import net.jbock.convert.matcher.Matcher;
import net.jbock.convert.reference.ReferenceTool;
import net.jbock.convert.reference.StringConverterType;
import net.jbock.either.Either;
import net.jbock.parameter.AbstractItem;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;
import net.jbock.validate.ParameterStyle;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.either.Either.left;

@ParameterScope
public class ConverterValidator extends MatchValidator {

  private final List<Matcher> matchers;
  private final ReferenceTool referenceTool;
  private final Util util;
  private final SourceMethod sourceMethod;
  private final SourceElement sourceElement;
  private final Types types;

  @Inject
  ConverterValidator(
      List<Matcher> matchers,
      ReferenceTool referenceTool,
      Util util,
      SourceMethod sourceMethod,
      SourceElement sourceElement,
      ParameterStyle parameterStyle,
      Types types) {
    super(parameterStyle);
    this.matchers = matchers;
    this.referenceTool = referenceTool;
    this.util = util;
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.types = types;
  }

  public <P extends AbstractItem> Either<String, Mapped<P>> validate(
      P parameter,
      TypeElement converter) {
    Optional<String> maybeFailure = util.commonTypeChecks(converter)
        .or(() -> checkNotAbstract(converter))
        .or(() -> checkNoTypevars(converter))
        .or(() -> checkConverterAnnotationPresent(converter));
    return Either.unbalancedLeft(maybeFailure)
        .flatMap(() -> referenceTool.getReferencedType(converter))
        .flatMap(functionType -> tryAllMatchers(functionType, parameter, converter));
  }

  private <P extends AbstractItem> Either<String, Mapped<P>> tryAllMatchers(
      StringConverterType functionType,
      P parameter,
      TypeElement converter) {
    List<Match> matches = new ArrayList<>();
    for (Matcher matcher : matchers) {
      Optional<Match> match = matcher.tryMatch(parameter);
      match.ifPresent(matches::add);
      match = match.filter(m -> isValidMatch(m, functionType));
      if (match.isPresent()) {
        Match m = match.get();
        return Either.unbalancedLeft(validateMatch(m))
            .orElseRight(() -> getMapExpr(functionType, converter))
            .map(code -> new MapExpr(code, m.baseType(), false))
            .map(mapExpr -> m.toConvertedParameter(mapExpr, parameter));
      }
    }
    TypeMirror typeForErrorMessage = matches.stream()
        .max(Comparator.comparing(Match::multiplicity))
        .map(Match::baseType)
        .orElse(sourceMethod.returnType());
    return left(noMatchError(typeForErrorMessage));
  }

  private Optional<String> checkConverterAnnotationPresent(TypeElement converter) {
    Converter converterAnnotation = converter.getAnnotation(Converter.class);
    boolean nestedMapper = util.getEnclosingElements(converter).contains(sourceElement.element());
    if (converterAnnotation == null && !nestedMapper) {
      return Optional.of("converter must be an inner class of the command class, or carry the @"
          + Converter.class.getSimpleName() + " annotation");
    }
    return Optional.empty();
  }

  private Optional<String> checkNotAbstract(TypeElement converter) {
    if (converter.getModifiers().contains(ABSTRACT)) {
      return Optional.of("converter class may not be abstract");
    }
    return Optional.empty();
  }

  private Optional<String> checkNoTypevars(TypeElement converter) {
    if (!converter.getTypeParameters().isEmpty()) {
      return Optional.of("type parameters are not allowed in converter class declaration");
    }
    return Optional.empty();
  }

  private CodeBlock getMapExpr(StringConverterType functionType, TypeElement converter) {
    if (functionType.isSupplier()) {
      return CodeBlock.of("new $T().get()", converter.asType());
    }
    return CodeBlock.of("new $T()", converter.asType());
  }

  private boolean isValidMatch(Match match, StringConverterType functionType) {
    return types.isSameType(functionType.outputType(), match.baseType());
  }

  private String noMatchError(TypeMirror type) {
    return "converter should extend StringConverter<" + util.typeToString(type) + ">";
  }
}
