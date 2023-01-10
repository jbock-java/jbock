package net.jbock.annotated;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static net.jbock.common.Constants.instancesOf;

public final class Items {

    private static final Comparator<Parameter> INDEX_COMPARATOR = comparingInt(Parameter::index);

    private final List<Option> namedOptions;
    private final List<Parameter> positionalParameters;
    private final List<VarargsParameter> varargsParameters;

    private Items(List<Option> namedOptions,
                  List<Parameter> positionalParameters,
                  List<VarargsParameter> varargsParameters) {
        this.namedOptions = namedOptions;
        this.positionalParameters = positionalParameters;
        this.varargsParameters = varargsParameters;
    }

    static Items createItems(List<? extends Item> itemList) {
        return new Items(itemList.stream()
                .flatMap(instancesOf(Option.class))
                .collect(toList()),
                itemList.stream()
                        .flatMap(instancesOf(Parameter.class))
                        .sorted(INDEX_COMPARATOR)
                        .collect(toList()),
                itemList.stream()
                        .flatMap(instancesOf(VarargsParameter.class))
                        .collect(toList()));
    }

    public List<Option> namedOptions() {
        return namedOptions;
    }

    public List<Parameter> positionalParameters() {
        return positionalParameters;
    }

    public List<VarargsParameter> varargsParameters() {
        return varargsParameters;
    }
}
