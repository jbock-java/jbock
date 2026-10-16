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

    static Items createItems(List<? extends SimpleItem> itemList) {
        List<SimpleOption> options = itemList.stream()
                .flatMap(instancesOf(SimpleOption.class))
                .toList();
        List<SimpleParameter> parameters = itemList.stream()
                .flatMap(instancesOf(SimpleParameter.class))
                .toList();
        List<Option> indexedOptions = new ArrayList<>(options.size());
        List<Parameter> indexedParameters = new ArrayList<>(parameters.size());
        for (int i = 0; i < options.size(); i++) {
            SimpleOption option = options.get(i);
            indexedOptions.add(Option.createOption(option, i));
        }
        for (int i = 0; i < parameters.size(); i++) {
            SimpleParameter parameter = parameters.get(i);
            indexedParameters.add(Parameter.createParameter(parameter, i));
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
