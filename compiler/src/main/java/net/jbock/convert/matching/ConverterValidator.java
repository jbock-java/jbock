package net.jbock.convert.matching;

import com.squareup.javapoet.CodeBlock;
import net.jbock.Converter;
import net.jbock.common.TypeTool;
import net.jbock.common.Util;
import net.jbock.compiler.SourceElement;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.ParameterScope;
import net.jbock.convert.matching.matcher.Matcher;
import net.jbock.convert.reference.FunctionType;
import net.jbock.convert.reference.ReferenceTool;
import net.jbock.either.Either;
import net.jbock.parameter.AbstractParameter;
import net.jbock.parameter.ParameterStyle;
import net.jbock.validate.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
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
  private final TypeTool tool;

  @Inject
  ConverterValidator(
      List<Matcher> matchers,
      ReferenceTool referenceTool,
      Util util,
      SourceMethod sourceMethod,
      SourceElement sourceElement,
      TypeTool tool,
      ParameterStyle parameterStyle) {
    super(parameterStyle);
    this.matchers = matchers;
    this.referenceTool = referenceTool;
    this.util = util;
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.tool = tool;
  }

  public <P extends AbstractParameter> Either<String, ConvertedParameter<P>> validate(
      P parameter,
      TypeElement converter) {
    Optional<String> maybeFailure = util.commonTypeChecks(converter)
        .or(() -> checkNotAnInterface(converter))
        .or(() -> checkNotAbstract(converter))
        .or(() -> checkNoTypevars(converter))
        .or(() -> checkConverterAnnotationPresent(converter));
    return Either.unbalancedLeft(maybeFailure)
        .flatMap(() -> referenceTool.getReferencedType(converter))
        .flatMap(functionType -> tryAllMatchers(functionType, parameter, converter));
  }

  private <P extends AbstractParameter> Either<String, ConvertedParameter<P>> tryAllMatchers(
      FunctionType functionType,
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
            .orElseRight(() -> CodeBlock.builder()
                .add(".map(")
                .add(getMapExpr(functionType, converter))
                .add(")").build())
            .map(mapExpr -> m.toConvertedParameter(Optional.of(mapExpr), parameter));
      }
    }
    TypeMirror typeForErrorMessage = matches.stream()
        .max(Comparator.comparing(Match::skew))
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

  private CodeBlock getMapExpr(FunctionType functionType, TypeElement converter) {
    if (functionType.isSupplier()) {
      return CodeBlock.of("new $T().get()", converter.asType());
    }
    return CodeBlock.of("new $T()", converter.asType());
  }

  private boolean isValidMatch(Match match, FunctionType functionType) {
    return tool.isSameType(functionType.outputType(), match.baseType());
  }

  private String noMatchError(TypeMirror type) {
    return "converter should implement Function<String, " + util.typeToString(type) + ">";
  }

  private Optional<? extends String> checkNotAnInterface(TypeElement converter) {
    if (converter.getKind() == ElementKind.INTERFACE) {
      return Optional.of("converter cannot be an interface");
    }
    return Optional.empty();
  }
}
