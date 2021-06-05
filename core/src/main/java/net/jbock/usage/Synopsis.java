package net.jbock.usage;

import net.jbock.model.Item;
import net.jbock.model.Option;
import net.jbock.model.Parameter;
import net.jbock.model.Skew;
import net.jbock.model.CommandModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.jbock.model.Skew.OPTIONAL;
import static net.jbock.model.Skew.REPEATABLE;
import static net.jbock.model.Skew.REQUIRED;

/**
 * Creates a single-line usage summary
 */
public class Synopsis {

  private final List<Option> options;
  private final List<Parameter> parameters;
  private final String programName;

  Synopsis(List<Option> options, List<Parameter> parameters, String programName) {
    this.options = options;
    this.parameters = parameters;
    this.programName = programName;
  }

  public static Synopsis create(CommandModel context) {
    return new Synopsis(context.options(), context.parameters(), context.programName());
  }

  /**
   * Public method that may be invoked from the generated code.
   */
  public List<String> usage(String prefix) {
    List<String> result = new ArrayList<>();
    result.add(prefix);
    result.add(programName);
    if (optionalOptions().findAny().isPresent()) {
      result.add("[OPTION]...");
    }
    for (Option option : requiredOptions()) {
      String firstName = option.names().get(0);
      result.add(String.format("%s %s", firstName, option.paramLabel()));
    }
    for (Parameter param : regularParameters()) {
      Skew skew = param.skew();
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
    repeatableParameter().ifPresent(param -> result.add("[" + param.paramLabel() + "]"));
    return result;
  }

  private Stream<Option> optionalOptions() {
    return filterBySkew(options, OPTIONAL);
  }

  private List<Option> requiredOptions() {
    return filterBySkew(options, REQUIRED).collect(Collectors.toList());
  }

  private List<Parameter> regularParameters() {
    return filterBySkew(parameters, sk -> sk != REPEATABLE).collect(Collectors.toList());
  }

  private Optional<Parameter> repeatableParameter() {
    return filterBySkew(parameters, REPEATABLE).findAny();
  }

  private static <E extends Item> Stream<E> filterBySkew(List<E> items, Skew skew) {
    return filterBySkew(items, sk -> sk == skew);
  }

  private static <E extends Item> Stream<E> filterBySkew(List<E> items, Predicate<Skew> p) {
    return items.stream().filter(o -> p.test(o.skew()));
  }
}
