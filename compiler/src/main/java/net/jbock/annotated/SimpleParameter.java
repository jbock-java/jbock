package net.jbock.annotated;

import javax.lang.model.element.ExecutableElement;

record SimpleParameter(
        ExecutableElement method,
        net.jbock.Parameter parameter,
        String safeName) implements SimpleItem {
}
