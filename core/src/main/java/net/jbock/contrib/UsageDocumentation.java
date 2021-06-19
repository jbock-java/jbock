package net.jbock.contrib;

import net.jbock.model.CommandModel;
import net.jbock.model.Item;
import net.jbock.model.Option;
import net.jbock.model.Parameter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

final class UsageDocumentation {

  private static final int CONTINUATION_INDENT_USAGE = 8;

  private final PrintStream out;
  private final int terminalWidth;
  private final Map<String, String> messages;
  private final String descriptionKey;
  private final List<String> descriptionLines;
  private final List<Option> options;
  private final List<Parameter> parameters;
  private final Synopsis synopsis;
  private final AnsiStyle ansiStyle;
  private final int maxWidthOptions;
  private final int maxWidthParameters;

  private UsageDocumentation(
      PrintStream out,
      int terminalWidth,
      Map<String, String> messages,
      String descriptionKey,
      List<String> descriptionLines,
      List<Option> options,
      List<Parameter> parameters,
      Synopsis synopsis,
      AnsiStyle ansiStyle,
      int maxWidthOptions,
      int maxWidthParameters) {
    this.descriptionKey = descriptionKey;
    this.descriptionLines = descriptionLines;
    this.out = out;
    this.terminalWidth = terminalWidth;
    this.messages = messages;
    this.options = options;
    this.parameters = parameters;
    this.synopsis = synopsis;
    this.ansiStyle = ansiStyle;
    this.maxWidthOptions = maxWidthOptions;
    this.maxWidthParameters = maxWidthParameters;
  }

  static Builder builder(CommandModel context) {
    return new Builder(context);
  }

  static final class Builder {

    private final CommandModel model;

    private PrintStream out = System.err;
    private int terminalWidth = 80;
    private boolean ansi = true;
    private Map<String, String> messages = Collections.emptyMap();

    private Builder(CommandModel model) {
      this.model = model;
    }

    Builder withTerminalWidth(int width) {
      this.terminalWidth = width == 0 ? this.terminalWidth : width;
      return this;
    }

    Builder withMessages(Map<String, String> map) {
      this.messages = map;
      return this;
    }

    Builder withAnsi(boolean ansi) {
      this.ansi = ansi;
      return this;
    }

    Builder withOutputStream(PrintStream out) {
      this.out = out;
      return this;
    }

    UsageDocumentation build() {
      return new UsageDocumentation(
          out, terminalWidth, messages,
          model.descriptionKey(),
          model.descriptionLines(),
          model.options(),
          model.parameters(),
          Synopsis.create(model),
          AnsiStyle.create(ansi),
          maxWidth(model.options()),
          maxWidth(model.parameters()));
    }

    private int maxWidth(List<? extends Item> items) {
      return items.stream()
          .map(Item::namesOverview)
          .mapToInt(String::length)
          .max()
          .orElse(0);
    }
  }

  void printUsageDocumentation() {
    List<String> description = new ArrayList<>();
    String desc = messages.get(descriptionKey);
    if (desc != null) {
      Collections.addAll(description, desc.split("\\s+", -1));
    } else {
      for (String line : descriptionLines) {
        Collections.addAll(description, line.split("\\s+", -1));
      }
    }
    makeLines("", description).forEach(out::println);

    if (!description.isEmpty()) {
      out.println();
    }

    String optionsFormat = "  %1$-" + maxWidthOptions + "s ";
    String paramsFormat = "  %1$-" + maxWidthParameters + "s ";
    String indent_p = String.join("", Collections.nCopies(maxWidthParameters + 4, " "));
    String indent_o = String.join("", Collections.nCopies(maxWidthOptions + 4, " "));

    out.println(ansiStyle.bold("USAGE").orElse("USAGE"));
    String indent_u = String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " "));
    makeLines(indent_u, synopsis.createSynopsis(" ")).forEach(out::println);
    if (!parameters.isEmpty()) {
      out.println();
      out.println(ansiStyle.bold("PARAMETERS").orElse("PARAMETERS"));
    }
    for (Parameter parameter : parameters) {
      printItemDocumentation(parameter, String.format(paramsFormat, parameter.namesOverview()), indent_p);
    }
    if (!options.isEmpty()) {
      out.println();
      out.println(ansiStyle.bold("OPTIONS").orElse("OPTIONS"));
    }
    for (Option option : options) {
      printItemDocumentation(option, String.format(optionsFormat, option.namesOverview()), indent_o);
    }
  }

  private void printItemDocumentation(Item item, String itemName, String indent) {
    String message = item.descriptionKey().isEmpty() ? null : messages.get(item.descriptionKey());
    List<String> tokens = new ArrayList<>();
    tokens.add(itemName);
    tokens.addAll(Optional.ofNullable(message)
        .map(String::trim)
        .map(s -> s.split("\\s+", -1))
        .map(Arrays::asList)
        .orElseGet(() -> item.description().stream()
            .map(s -> s.split("\\s+", -1))
            .flatMap(Arrays::stream)
            .collect(Collectors.toList())));
    makeLines(indent, tokens).forEach(out::println);
  }

  private List<String> makeLines(String indent, List<String> tokens) {
    List<String> result = new ArrayList<>();
    StringBuilder line = new StringBuilder();
    int i = 0;
    while (i < tokens.size()) {
      String token = tokens.get(i);
      boolean fresh = line.length() == 0;
      if (!fresh && token.length() + line.length() + 1 > terminalWidth) {
        result.add(line.toString());
        line.setLength(0);
        continue;
      }
      if (i > 0) {
        line.append(fresh ? indent : " ");
      }
      line.append(token);
      i++;
    }
    if (line.length() > 0) {
      result.add(line.toString());
    }
    return result;
  }
}
