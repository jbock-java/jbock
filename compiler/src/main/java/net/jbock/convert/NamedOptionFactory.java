package net.jbock.convert;

import com.squareup.javapoet.CodeBlock;
import net.jbock.util.StringConverter;
import net.jbock.common.EnumName;
import net.jbock.common.ValidationFailure;
import net.jbock.either.Either;
import net.jbock.parameter.NamedOption;
import net.jbock.parameter.SourceMethod;
import net.jbock.processor.SourceElement;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.lang.Character.isWhitespace;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

@ParameterScope
public class NamedOptionFactory {

  // visible for testing
  static final Comparator<String> UNIX_NAMES_FIRST_COMPARATOR = (n1, n2) -> {
    boolean unix1 = n1.length() == 2;
    boolean unix2 = n2.length() == 2;
    if (unix1 && !unix2) {
      return -1;
    }
    if (!unix1 && unix2) {
      return 1;
    }
    return n1.compareTo(n2);
  };

  private final ConverterFinder converterFinder;
  private final ConverterClass converterClass;
  private final SourceMethod sourceMethod;
  private final SourceElement sourceElement;
  private final EnumName enumName;
  private final List<ConvertedParameter<NamedOption>> alreadyCreated;

  @Inject
  NamedOptionFactory(
      ConverterClass converterClass,
      ConverterFinder converterFinder,
      SourceMethod sourceMethod,
      SourceElement sourceElement,
      EnumName enumName,
      List<ConvertedParameter<NamedOption>> alreadyCreated) {
    this.converterFinder = converterFinder;
    this.converterClass = converterClass;
    this.sourceMethod = sourceMethod;
    this.sourceElement = sourceElement;
    this.enumName = enumName;
    this.alreadyCreated = alreadyCreated;
  }

  public Either<ValidationFailure, ConvertedParameter<NamedOption>> createNamedOption() {
    return checkOptionNames()
        .map(this::createNamedOption)
        .flatMap(namedOption -> {
          if (!converterClass.isPresent() && sourceMethod.returnType().getKind() == BOOLEAN) {
            return right(createFlag(namedOption));
          }
          return converterFinder.findConverter(namedOption);
        })
        .mapLeft(sourceMethod::fail);
  }

  private Either<String, List<String>> checkOptionNames() {
    if (sourceMethod.names().isEmpty()) {
      return left("define at least one option name");
    }
    for (ConvertedParameter<NamedOption> c : alreadyCreated) {
      for (String name : sourceMethod.names()) {
        for (String previousName : c.parameter().names()) {
          if (name.equals(previousName)) {
            return left("duplicate option name: " + name);
          }
        }
      }
    }
    List<String> result = new ArrayList<>();
    for (String name : sourceMethod.names()) {
      Optional<String> check = checkName(name);
      if (check.isPresent()) {
        return Either.unbalancedLeft(check).orElseRight(List::of);
      }
      if (result.contains(name)) {
        return left("duplicate option name: " + name);
      }
      result.add(name);
    }
    result.sort(UNIX_NAMES_FIRST_COMPARATOR);
    return right(result);
  }

  private Optional<String> checkName(String name) {
    if (Objects.toString(name, "").length() <= 1 || "--".equals(name)) {
      return Optional.of("invalid name: " + name);
    }
    if (!name.startsWith("-")) {
      return Optional.of("the name must start with a dash character: " + name);
    }
    if (name.startsWith("---")) {
      return Optional.of("the name must start with one or two dashes, not three:" + name);
    }
    if (!name.startsWith("--") && name.length() > 2) {
      return Optional.of("single-dash names must be single-character names: " + name);
    }
    if (sourceElement.helpEnabled() && "--help".equals(name)) {
      return Optional.of("'--help' is reserved, set 'helpEnabled=false' to allow it");
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (isWhitespace(c)) {
        return Optional.of("the name contains whitespace characters: " + name);
      }
      if (c == '=') {
        return Optional.of("the name contains '=': " + name);
      }
    }
    return Optional.empty();
  }

  private NamedOption createNamedOption(List<String> names) {
    return new NamedOption(enumName, names, sourceMethod);
  }

  private ConvertedParameter<NamedOption> createFlag(NamedOption namedOption) {
    CodeBlock mapExpr = CodeBlock.of(".map($T.create($T.identity()))", StringConverter.class, Function.class);
    return ConvertedParameter.create(mapExpr,
        Optional.empty(), Skew.FLAG,
        namedOption);
  }
}
