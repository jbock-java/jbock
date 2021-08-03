package net.jbock.annotated;

import net.jbock.common.EnumName;

import javax.lang.model.element.Name;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ExecutableElements {

    private final List<SimpleAnnotated> executableElements;
    private final Map<Name, EnumName> enumNames;

    private ExecutableElements(
            List<SimpleAnnotated> executableElements,
            Map<Name, EnumName> enumNames) {
        this.executableElements = executableElements;
        this.enumNames = enumNames;
    }

    static ExecutableElements create(List<SimpleAnnotated> abstractMethods) {
        Map<Name, EnumName> enumNames = createEnumNames(abstractMethods);
        return new ExecutableElements(abstractMethods, enumNames);
    }

    private static Map<Name, EnumName> createEnumNames(List<SimpleAnnotated> methods) {
        Set<EnumName> names = new HashSet<>();
        Map<Name, EnumName> result = new HashMap<>();
        for (SimpleAnnotated method : methods) {
            EnumName enumName = EnumName.create(method.simpleName().toString());
            while (names.contains(enumName)) {
                enumName = enumName.makeLonger();
            }
            names.add(enumName);
            result.put(method.simpleName(), enumName);
        }
        return result;
    }

    List<SimpleAnnotated> executableElements() {
        return executableElements;
    }

    Map<Name, EnumName> enumNames() {
        return enumNames;
    }
}
