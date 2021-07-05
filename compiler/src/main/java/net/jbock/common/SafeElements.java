package net.jbock.common;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Objects;
import java.util.Optional;

public class SafeElements {

    private final Elements elements;

    public SafeElements(Elements elements) {
        this.elements = elements;
    }

    public Optional<TypeElement> getTypeElement(String name) {
        return Optional.ofNullable(elements.getTypeElement(name));
    }

    public Optional<String> getDocComment(Element e) {
        String docComment = Objects.toString(elements.getDocComment(e), "");
        if (docComment.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(docComment);
    }
}
