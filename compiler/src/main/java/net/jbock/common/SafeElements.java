package net.jbock.common;

import io.jbock.simple.Inject;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Optional;

/**
 * A wrapper around {@link Elements} where none of the methods can return {@code null}.
 */
public class SafeElements {

    private final Elements elements;

    @Inject
    public SafeElements(ProcessingEnvironment processingEnvironment) {
        this.elements = processingEnvironment.getElementUtils();
    }

    public Optional<TypeElement> getTypeElement(String name) {
        return Optional.ofNullable(elements.getTypeElement(name));
    }
}
