package net.jbock.annotated;

import net.jbock.common.EnumName;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class ExecutableElements {

    private final List<ExecutableElement> executableElements;
    private final Map<Name, EnumName> enumNames;

    private ExecutableElements(
            List<ExecutableElement> executableElements,
            Map<Name, EnumName> enumNames) {
        this.executableElements = executableElements;
        this.enumNames = enumNames;
    }

    static ExecutableElements create(List<ExecutableElement> abstractMethods) {
        Map<Name, EnumName> enumNames = createEnumNames(abstractMethods);
        return new ExecutableElements(abstractMethods, enumNames);
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

    List<ExecutableElement> executableElements() {
        return executableElements;
    }

    Map<Name, EnumName> enumNames() {
        return enumNames;
    }
}
