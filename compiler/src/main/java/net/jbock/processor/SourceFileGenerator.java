package net.jbock.processor;

import com.palantir.javapoet.JavaFile;
import io.jbock.simple.Inject;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static javax.tools.Diagnostic.Kind.ERROR;

@Inject
record SourceFileGenerator(ProcessingEnvironment env) {

    void write(SourceElement sourceElement, JavaFile javaFile) {
        try {
            javaFile.writeTo(env.getFiler());
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stack = sw.toString();
            env.getMessager().printMessage(ERROR, stack, sourceElement.element());
        }
    }
}
