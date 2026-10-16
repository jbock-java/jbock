package net.jbock.annotated;

import javax.lang.model.element.ExecutableElement;

record SimpleOption(
        ExecutableElement method,
        net.jbock.Option option,
        String safeName) implements SimpleItem {
}
