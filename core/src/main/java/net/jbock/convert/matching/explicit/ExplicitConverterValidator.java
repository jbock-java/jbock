package net.jbock.convert.matching.explicit;

import com.squareup.javapoet.CodeBlock;
import net.jbock.Converter;
import net.jbock.compiler.TypeTool;
import net.jbock.compiler.parameter.AbstractParameter;
import net.jbock.compiler.parameter.ParameterStyle;
import net.jbock.convert.ConvertedParameter;
import net.jbock.convert.Util;
import net.jbock.convert.matching.ConverterValidator;
import net.jbock.convert.matching.Match;
import net.jbock.convert.matching.matcher.Matcher;
import net.jbock.convert.reference.FunctionType;
import net.jbock.convert.reference.ReferenceTool;
import net.jbock.either.Either;
import net.jbock.qualifier.SourceElement;
import net.jbock.qualifier.SourceMethod;

import javax.inject.Inject;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static net.jbock.either.Either.left;

public class ExplicitConverterValidator extends ConverterValidator {

  private final List<Matcher> matchers;
  private final ReferenceTool referenceTool;
  private final Util util;
  private final SourceMethod sourceMethod;
  private final SourceElement sourceElement;
  private final TypeTool tool;

  @Inject
  ExplicitConverterValidator(
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
    Optional<String> maybeFailure = util.commonTypeChecks(converter).map(s -> "converter " + s);
    return Either.ofLeft(maybeFailure).orRight(null)
        .filter(nothing -> checkNotAbstract(converter))
        .filter(nothing -> checkNoTypevars(converter))
        .filter(nothing -> checkMapperAnnotation(converter))
        .flatMap(nothing -> referenceTool.getReferencedType(converter))
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
        return Either.ofLeft(validateMatch(m)).orRight(null)
            .map(nothing -> CodeBlock.builder()
                .add(".map(")
                .add(getMapExpr(functionType, converter))
                .add(")").build())
            .map(mapExpr -> m.toCoercion(mapExpr, parameter));
      }
    }
    TypeMirror bestReturnType = matches.stream()
        .max(Comparator.comparing(Match::skew))
        .map(Match::baseReturnType)
        .orElse(sourceMethod.returnType());
    return left(ExplicitConverterValidator.noMatchError(bestReturnType));
  }

  private Optional<String> checkMapperAnnotation(TypeElement converter) {
    Converter converterAnnotation = converter.getAnnotation(Converter.class);
    boolean nestedMapper = util.getEnclosingElements(converter).contains(sourceElement.element());
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
    return tool.isSameType(functionType.outputType(), match.baseReturnType());
  }

  private static String noMatchError(TypeMirror type) {
    return "converter should implement Function<String, " + Util.typeToString(type) + ">";
  }
}