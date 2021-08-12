package net.jbock.common;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;

import static javax.tools.Diagnostic.Kind.ERROR;

public class ValidationFailure {

    private final Element about;
    private final String message;

    public ValidationFailure(String message, Element about) {
        this.message = message;
        this.about = about;
    }

    public ValidationFailure prepend(String prefix) {
        return new ValidationFailure(prefix + message, about);
    }

    public ValidationFailure about(Element about) {
        return new ValidationFailure(message, about);
    }

    public void writeTo(Messager messager) {
        messager.printMessage(ERROR, message, about);
    }
}
