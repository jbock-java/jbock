package net.jbock.annotated;

import javax.lang.model.element.Name;
import java.util.List;
import java.util.Map;

final class EnumNames {

    private final Builder builder;
    private final Map<Name, String> enumNames;

    private EnumNames(Builder builder, Map<Name, String> enumNames) {
        this.builder = builder;
        this.enumNames = enumNames;
    }

    static Builder builder(List<Executable> methods) {
        return new Builder(methods);
    }

    static final class Builder {

        private final List<Executable> methods;

        private Builder(List<Executable> methods) {
            this.methods = methods;
        }

        List<Executable> methods() {
            return methods;
        }

        EnumNames withEnumNames(Map<Name, String> enumNames) {
            return new EnumNames(this, enumNames);
        }
    }

    String enumNameFor(Name sourceMethodName) {
        return enumNames.get(sourceMethodName);
    }

    List<Executable> methods() {
        return builder.methods;
    }
}
