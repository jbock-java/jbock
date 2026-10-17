package net.jbock.annotated;

import java.util.ArrayList;
import java.util.List;

import static net.jbock.annotated.Option.createOption;
import static net.jbock.annotated.Parameter.createParameter;

public record Items(
        List<Option> namedOptions,
        List<Parameter> positionalParameters) {

    static Items createItems(List<? extends SimpleItem> itemList) {
        List<Option> options = new ArrayList<>(itemList.size());
        List<Parameter> parameters = new ArrayList<>(itemList.size());
        int io = 0, ip = 0;
        for (SimpleItem simpleItem : itemList) {
            switch (simpleItem) {
                case SimpleOption o -> options.add(createOption(o, io++));
                case SimpleParameter p -> parameters.add(createParameter(p, ip++));
            }
        }
        return new Items(options, parameters);
    }
}
