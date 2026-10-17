package net.jbock.annotated;

import javax.lang.model.element.ExecutableElement;

sealed interface SimpleItem permits SimpleOption, SimpleParameter {

    ExecutableElement method();

    String safeName();
}
