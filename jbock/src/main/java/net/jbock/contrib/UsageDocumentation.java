package net.jbock.contrib;

import net.jbock.ColorMode;
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
import java.util.regex.Pattern;

record UsageDocumentation(
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

    private static final int CONTINUATION_INDENT_USAGE = 8;

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    static Builder builder(CommandModel context) {
        return new Builder(context);
    }

    static final class Builder {

        private final CommandModel model;

        private PrintStream out = System.err;
        private int terminalWidth = 80;
        private ColorMode colorMode = ColorMode.AUTO;
        private Map<String, String> messages = Map.of();

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

        Builder withColorMode(ColorMode colorMode) {
            this.colorMode = colorMode;
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
                    AnsiStyle.create(colorMode),
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
            Collections.addAll(description, WHITESPACE.split(desc, -1));
        } else {
            for (String line : descriptionLines) {
                Collections.addAll(description, WHITESPACE.split(line, -1));
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

        out.println(ansiStyle.bold("USAGE"));
        String indent_u = String.join("", Collections.nCopies(CONTINUATION_INDENT_USAGE, " "));
        makeLines(indent_u, synopsis.createSynopsis(" ")).forEach(out::println);
        if (!parameters.isEmpty()) {
            out.println();
            out.println(ansiStyle.bold("PARAMETERS"));
        }
        for (Parameter parameter : parameters) {
            printItemDocumentation(parameter, String.format(paramsFormat, parameter.namesOverview()), indent_p);
        }
        if (!options.isEmpty()) {
            out.println();
            out.println(ansiStyle.bold("OPTIONS"));
        }
        for (Option option : options) {
            printItemDocumentation(option, String.format(optionsFormat, option.namesOverview()), indent_o);
        }
    }

    private void printItemDocumentation(Item item, String itemName, String indent) {
        Optional<String> message = item.descriptionKey().isEmpty() ?
                Optional.empty() :
                Optional.ofNullable(messages.get(item.descriptionKey()));
        List<String> tokens = new ArrayList<>();
        tokens.add(itemName);
        tokens.addAll(message
                .map(String::trim)
                .map(s -> WHITESPACE.split(s, -1))
                .map(Arrays::asList)
                .orElseGet(() -> item.description().stream()
                        .map(s -> WHITESPACE.split(s, -1))
                        .flatMap(Arrays::stream)
                        .toList()));
        makeLines(indent, tokens).forEach(out::println);
    }

    private List<String> makeLines(String indent, List<String> tokens) {
        List<String> result = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        int i = 0;
        while (i < tokens.size()) {
            String token = tokens.get(i);
            boolean fresh = line.isEmpty();
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
        if (!line.isEmpty()) {
            result.add(line.toString());
        }
        return result;
    }
}
