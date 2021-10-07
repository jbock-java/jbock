package net.jbock.annotated;

import net.jbock.Option;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings(value = {"rawtypes", "unchecked"})
class SimpleAnnotatedTest {

    @Test
    void create() {
        ExecutableElement executableElement = mock(ExecutableElement.class);
        Annotation annotation = mock(SuppressWarnings.class);
        Class optionClass = Option.class;
        when(annotation.annotationType()).thenReturn(optionClass);
        assertThrows(AssertionError.class, () ->
                Executable.create(executableElement, annotation));
    }
}