package net.jbock.annotated;

import net.jbock.processor.SourceElement;
import org.junit.jupiter.api.Test;

import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SimpleAnnotatedTest {

    @Test
    void create() {
        ExecutableElement executableElement = mock(ExecutableElement.class);
        Annotation annotation = mock(SuppressWarnings.class);
        SourceElement sourceElement = mock(SourceElement.class);
        assertThrows(AssertionError.class, () ->
                Executable.create(sourceElement, executableElement, annotation));
    }
}