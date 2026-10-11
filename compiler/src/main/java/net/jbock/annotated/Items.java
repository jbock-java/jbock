package net.jbock.annotated;

import java.util.ArrayList;
import java.util.List;

import static net.jbock.common.Constants.instancesOf;

public final class Items {

    private final List<Option> namedOptions;
    private final List<Parameter> positionalParameters;

    private Items(List<Option> namedOptions,
                  List<Parameter> positionalParameters) {
        this.namedOptions = namedOptions;
        this.positionalParameters = positionalParameters;
    }

    static Items createItems(List<? extends Item> itemList) {
        List<Option> options = itemList.stream()
                .flatMap(instancesOf(Option.class))
                .toList();
        List<Parameter> parameters = itemList.stream()
                .flatMap(instancesOf(Parameter.class))
                .toList();
        List<Option> indexedOptions = new ArrayList<>(options.size());
        List<Parameter> indexedParameters = new ArrayList<>(parameters.size());
        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);
            indexedOptions.add(option.withInternalIndex(i));
        }
        for (int i = 0; i < parameters.size(); i++) {
            Parameter option = parameters.get(i);
            indexedParameters.add(option.withInternalIndex(i));
        }
        return new Items(indexedOptions, indexedParameters);
    }

    public List<Option> namedOptions() {
        return namedOptions;
    }

    public List<Parameter> positionalParameters() {
        return positionalParameters;
    }
}
