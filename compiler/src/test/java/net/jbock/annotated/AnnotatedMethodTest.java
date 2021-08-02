package net.jbock.annotated;

import net.jbock.common.EnumName;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AnnotatedMethodTest {

    @Test
    void create() {
        ExecutableElement executableElement = mock(ExecutableElement.class);
        Annotation annotation = mock(SuppressWarnings.class);
        assertThrows(AssertionError.class, () ->
                AnnotatedMethod.create(executableElement, annotation, EnumName.create("a")));
    }
}