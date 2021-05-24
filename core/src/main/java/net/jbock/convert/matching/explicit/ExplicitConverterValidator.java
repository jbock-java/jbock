package net.jbock.convert.matching.explicit;

import com.squareup.javapoet.CodeBlock;
import dagger.Reusable;
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
import static net.jbock.either.Either.right;

@Reusable
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
        .flatMap(nothing -> checkNotAbstract(converter))
        .flatMap(nothing -> checkNoTypevars(converter))
        .flatMap(nothing -> checkMapperAnnotation(converter))
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
            .map(mapExpr -> m.toConvertedParameter(mapExpr, parameter));
      }
    }
    TypeMirror typeForErrorMessage = matches.stream()
        .max(Comparator.comparing(Match::skew))
        .map(Match::baseType)
        .orElse(sourceMethod.returnType());
    return left(ExplicitConverterValidator.noMatchError(typeForErrorMessage));
  }

  private Either<String, Void> checkMapperAnnotation(TypeElement converter) {
    Converter converterAnnotation = converter.getAnnotation(Converter.class);
    boolean nestedMapper = util.getEnclosingElements(converter).contains(sourceElement.element());
    if (converterAnnotation == null && !nestedMapper) {
      return left("converter must be an inner class of the command class, or carry the @" + Converter.class.getSimpleName() + " annotation");
    }
    return right(null);
  }

  private Either<String, Void> checkNotAbstract(TypeElement converter) {
    if (converter.getModifiers().contains(ABSTRACT)) {
      return left("non-abstract converter class");
    }
    return right(null);
  }

  private Either<String, Void> checkNoTypevars(TypeElement converter) {
    if (!converter.getTypeParameters().isEmpty()) {
      return left("found type parameters in converter class declaration");
    }
    return right(null);
  }

  private CodeBlock getMapExpr(FunctionType functionType, TypeElement converter) {
    return CodeBlock.of("new $T()$L", converter.asType(),
        functionType.isSupplier() ? ".get()" : "");
  }

  private boolean isValidMatch(Match match, FunctionType functionType) {
    return tool.isSameType(functionType.outputType(), match.baseType());
  }

  private static String noMatchError(TypeMirror type) {
    return "converter should implement Function<String, " + Util.typeToString(type) + ">";
  }
}
