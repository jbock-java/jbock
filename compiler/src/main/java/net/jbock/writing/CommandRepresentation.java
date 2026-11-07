package net.jbock.writing;

import net.jbock.annotated.Item;
import net.jbock.annotated.Option;
import net.jbock.annotated.Parameter;
import net.jbock.processor.SourceElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static net.jbock.common.Suppliers.memoize;

public final class CommandRepresentation {

    private final Supplier<List<Item>> allMappings = memoize(() -> {
        List<Item> result = new ArrayList<>(namedOptions().size() + positionalParameters().size());
        result.addAll(namedOptions());
        result.addAll(positionalParameters());
        return result;
    });

    private final List<Option> namedOptions;
    private final List<Parameter> positionalParameters;
    private final SourceElement sourceElement;

    public CommandRepresentation(
            SourceElement sourceElement,
            List<Option> namedOptions,
            List<Parameter> positionalParameters) {
        this.namedOptions = namedOptions;
        this.positionalParameters = positionalParameters;
        this.sourceElement = sourceElement;
    }

    SourceElement sourceElement() {
        return sourceElement;
    }

    List<Parameter> positionalParameters() {
        return positionalParameters;
    }

    List<Option> namedOptions() {
        return namedOptions;
    }

    List<Item> allMappings() {
        return allMappings.get();
    }

    boolean isSuperCommand() {
        return sourceElement().isSuperCommand();
    }

    boolean parseOrExitMethodAcceptsList() {
        return sourceElement().parseOrExitMethodAcceptsList();
    }

    boolean enableAtFileExpansion() {
        return sourceElement().enableAtFileExpansion();
    }
}
