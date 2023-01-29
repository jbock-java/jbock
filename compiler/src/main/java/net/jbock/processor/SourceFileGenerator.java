package net.jbock.processor;

import io.jbock.javapoet.JavaFile;
import io.jbock.simple.Inject;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static javax.tools.Diagnostic.Kind.ERROR;

final class SourceFileGenerator {

    private final Filer filer;
    private final Messager messager;

    @Inject
    SourceFileGenerator(ProcessingEnvironment processingEnvironment) {
        this.filer = processingEnvironment.getFiler();
        this.messager = processingEnvironment.getMessager();
    }

    void write(SourceElement sourceElement, JavaFile javaFile) {
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            messager.printMessage(ERROR, stack, sourceElement.element());
        }
    }
}
