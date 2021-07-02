package net.jbock.contrib;

import net.jbock.either.Optional;
import net.jbock.model.CommandModel;
import net.jbock.model.Item;
import net.jbock.model.Multiplicity;
import net.jbock.model.Option;
import net.jbock.model.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.jbock.model.Multiplicity.REPEATABLE;
import static net.jbock.model.Multiplicity.REQUIRED;

final class Synopsis {

  private final List<Option> options;
  private final List<Parameter> parameters;
  private final String programName;

  private Synopsis(
      String programName,
      List<Option> options,
      List<Parameter> parameters) {
    this.options = options;
    this.parameters = parameters;
    this.programName = programName;
  }

  static Synopsis create(CommandModel context) {
    return new Synopsis(
        context.programName(),
        context.options(),
        context.parameters());
  }

  List<String> createSynopsis(String prefix) {
    List<String> result = new ArrayList<>();
    result.add(prefix);
    result.add(programName);
    if (optionalOptions().findAny().isPresent()) {
      result.add("[OPTIONS]");
    }
    for (Option option : requiredOptions()) {
      String firstName = option.names().get(0);
      result.add(String.format("%s %s", firstName, option.paramLabel()));
    }
    for (Parameter param : regularParameters()) {
      Multiplicity skew = param.multiplicity();
      String paramLabel = param.paramLabel();
      switch (skew) {
        case OPTIONAL:
          result.add("[" + paramLabel + "]");
          break;
        case REQUIRED:
          result.add(paramLabel);
          break;
        default:
          throw new IllegalArgumentException("unexpected skew: " + skew);
      }
    }
    repeatableParameter().ifPresent(param -> result.add(param.paramLabel() + "..."));
    return result;
  }

  private Stream<Option> optionalOptions() {
    return filterBySkew(options, sk -> sk != REQUIRED);
  }

  private List<Option> requiredOptions() {
    return filterBySkew(options, REQUIRED).collect(Collectors.toList());
  }

  private List<Parameter> regularParameters() {
    return filterBySkew(parameters, sk -> sk != REPEATABLE).collect(Collectors.toList());
  }

  private Optional<Parameter> repeatableParameter() {
    return filterBySkew(parameters, REPEATABLE).findAny()
        .map(Optional::of)
        .orElse(Optional.empty());
  }

  private static <E extends Item> Stream<E> filterBySkew(List<E> items, Multiplicity skew) {
    return filterBySkew(items, sk -> sk == skew);
  }

  private static <E extends Item> Stream<E> filterBySkew(List<E> items, Predicate<Multiplicity> p) {
    return items.stream().filter(o -> p.test(o.multiplicity()));
  }
}
