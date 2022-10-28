package net.jbock.annotated;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static net.jbock.common.Constants.instancesOf;

final class ItemsBuilder {

    private static final Comparator<Parameter> BY_INDEX = Comparator.comparingInt(Parameter::index);

    private ItemsBuilder() {
    }

    static Items createItems(List<? extends Item> itemList) {
        Step1 builder = new Step1(itemList);
        return builder.withNamedOptions(builder.annotatedMethods()
                        .flatMap(instancesOf(Option.class))
                        .collect(toList()))
                .withPositionalParameters(builder.annotatedMethods()
                        .flatMap(instancesOf(Parameter.class))
                        .sorted(BY_INDEX)
                        .collect(toList()))
                .withVarargsParameters(builder.annotatedMethods()
                        .flatMap(instancesOf(VarargsParameter.class))
                        .collect(toList()));
    }

    private static final class Step1 {

        private final List<? extends Item> itemList;

        private Step1(List<? extends Item> itemList) {
            this.itemList = itemList;
        }

        Stream<? extends Item> annotatedMethods() {
            return itemList.stream();
        }

        Step2 withNamedOptions(List<Option> namedOptions) {
            return new Step2(this, namedOptions);
        }
    }

    static final class Step2 {

        final Step1 step1;
        final List<Option> namedOptions;

        private Step2(Step1 step1, List<Option> namedOptions) {
            this.step1 = step1;
            this.namedOptions = namedOptions;
        }

        Step3 withPositionalParameters(List<Parameter> positionalParameters) {
            return new Step3(this, positionalParameters);
        }
    }

    static final class Step3 {

        final Step2 step2;
        final List<Parameter> positionalParameters;

        private Step3(Step2 step2, List<Parameter> positionalParameters) {
            this.step2 = step2;
            this.positionalParameters = positionalParameters;
        }

        Items withVarargsParameters(List<VarargsParameter> varargsParameters) {
            return new Items(this, varargsParameters);
        }
    }
}
