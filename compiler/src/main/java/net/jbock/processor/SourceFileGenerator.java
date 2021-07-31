package net.jbock.processor;

import com.squareup.javapoet.JavaFile;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.inject.Inject;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

@ProcessorScope
public class SourceFileGenerator {

    private final Filer filer;
    private final Messager messager;

    @Inject
    SourceFileGenerator(Filer filer, Messager messager) {
        this.filer = filer;
        this.messager = messager;
    }

    void write(SourceElement sourceElement, JavaFile javaFile) {
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            messager.printMessage(Diagnostic.Kind.ERROR, stack, sourceElement.element());
        }
    }
}
