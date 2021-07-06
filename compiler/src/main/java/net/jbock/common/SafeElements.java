package net.jbock.common;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Optional;

public class SafeElements {

    private final Elements elements;

    public SafeElements(Elements elements) {
        this.elements = elements;
    }

    public Optional<TypeElement> getTypeElement(String name) {
        return Optional.ofNullable(elements.getTypeElement(name));
    }
}
