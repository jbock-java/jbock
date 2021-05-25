package net.jbock.convert;

import net.jbock.compiler.EnumName;
import net.jbock.compiler.SourceElement;
import net.jbock.compiler.ValidationFailure;
import net.jbock.either.Either;
import net.jbock.parameter.NamedOption;
import net.jbock.validate.SourceMethod;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.lang.Character.isWhitespace;
import static javax.lang.model.type.TypeKind.BOOLEAN;
import static net.jbock.either.Either.left;
import static net.jbock.either.Either.right;

@ParameterScope
public class NamedOptionFactory {

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
      Either<String, String> check = checkName(name);
      if (!check.isRight()) {
        return check.map(__ -> List.of());
      }
      if (result.contains(name)) {
        return left("duplicate option name: " + name);
      }
      result.add(name);
    }
    result.sort((n1, n2) -> {
      boolean unix1 = n1.length() == 2;
      boolean unix2 = n2.length() == 2;
      if (unix1 && !unix2) {
        return -1;
      }
      if (!unix1 && unix2) {
        return 1;
      }
      return n1.compareTo(n2);
    });
    return right(result);
  }

  private Either<String, String> checkName(String name) {
    if (Objects.toString(name, "").isEmpty()) {
      return left("empty name");
    }
    if (name.charAt(0) != '-') {
      return left("the name must start with a dash character: " + name);
    }
    if (name.startsWith("---")) {
      return left("the name must start with one or two dashes, not three:" + name);
    }
    if (name.equals("--")) {
      return left("not a valid name: --");
    }
    if (name.charAt(1) != '-' && name.length() >= 3) {
      return left("single-dash names must be single-character names: " + name);
    }
    if (sourceElement.helpEnabled()) {
      if ("--help".equals(name)) {
        return left("'--help' cannot be an option name, unless the help feature is disabled.");
      }
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (isWhitespace(c)) {
        return left("the name contains whitespace characters: " + name);
      }
      if (c == '=') {
        return left("the name contains '=': " + name);
      }
    }
    return right(name);
  }

  private NamedOption createNamedOption(List<String> names) {
    return new NamedOption(enumName, names, sourceMethod);
  }

  private ConvertedParameter<NamedOption> createFlag(NamedOption namedOption) {
    return ConvertedParameter.create(Optional.empty(), Optional.empty(), Skew.FLAG,
        enumName, namedOption);
  }
}
