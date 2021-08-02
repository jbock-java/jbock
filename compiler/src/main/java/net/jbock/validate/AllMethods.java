package net.jbock.validate;

import net.jbock.common.EnumName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AllMethods {

    private final List<ExecutableElement> abstractMethods;
    private final Map<Name, EnumName> enumNames;

    private AllMethods(
            List<ExecutableElement> abstractMethods,
            Map<Name, EnumName> enumNames) {
        this.abstractMethods = abstractMethods;
        this.enumNames = enumNames;
    }

    static AllMethods create(List<ExecutableElement> abstractMethods) {
        Map<Name, EnumName> enumNames = createEnumNames(abstractMethods);
        return new AllMethods(abstractMethods, enumNames);
    }

    private static Map<Name, EnumName> createEnumNames(List<ExecutableElement> methods) {
        Set<EnumName> names = new HashSet<>();
        Map<Name, EnumName> result = new HashMap<>();
        for (ExecutableElement method : methods) {
            EnumName enumName = EnumName.create(method.getSimpleName().toString());
            while (names.contains(enumName)) {
                enumName = enumName.makeLonger();
            }
            names.add(enumName);
            result.put(method.getSimpleName(), enumName);
        }
        return result;
    }

    List<ExecutableElement> abstractMethods() {
        return abstractMethods;
    }

    Map<Name, EnumName> enumNames() {
        return enumNames;
    }
}
