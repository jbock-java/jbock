package net.jbock.processor;

import io.jbock.javapoet.JavaFile;
import org.junit.jupiter.api.Test;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class SourceFileGeneratorTest {

    private final Filer filer = mock(Filer.class);
    private final Messager messager = spy(Messager.class);
    private final SourceFileGenerator sourceFileGenerator = new SourceFileGenerator(filer, messager);

    @Test
    void testWriteException() throws IOException {
        JavaFile javaFile = mock(JavaFile.class);
        doThrow(IOException.class).when(javaFile).writeTo(any(Filer.class));
        sourceFileGenerator.write(mock(SourceElement.class), javaFile);
        verify(messager).printMessage(eq(Diagnostic.Kind.ERROR), anyString(), any());
    }
}
